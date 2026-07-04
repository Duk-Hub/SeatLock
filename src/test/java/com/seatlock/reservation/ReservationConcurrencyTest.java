package com.seatlock.reservation;

import com.seatlock.IntegrationTest;
import com.seatlock.global.exception.BusinessException;
import com.seatlock.reservation.dto.ReservationCreateRequest;
import com.seatlock.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// 락 도입 전의 깨진 동작(데드락·오버부킹)을 의도적으로 실증하는 재현 테스트 — 결과: docs/concurrency/01-race-reproduction.md
@IntegrationTest
class ReservationConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ReservationConcurrencyTest.class);

    private static final int THREAD_COUNT = 32;

    @Autowired
    ReservationService reservationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBefore() {
        jdbcTemplate.update("DELETE FROM reservation_seat");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("UPDATE schedule_seat SET status = 'AVAILABLE'");
    }

    @Test
    @DisplayName("락이 없으면 같은 좌석 동시 예매가 데드락을 일으킨다")
    void deadlockWithoutLock() throws Exception {
        // given
        Long seatId = anyAvailableSeatId();

        // when
        StormResult result = reserveConcurrently(seatId);

        // then — 중복 예매는 FK 공유 락이 우연히 막지만, 동시 진입자 대부분이 409가 아닌 데드락(500) 예외를 받는다.
        int reservedRows = reservationSeatRows(seatId);
        log.info("[재현] 좌석 1개에 동시 예매 {}건 → 성공 {}건, 좌석충돌(409) {}건, 데드락 {}건, reservation_seat 행 {}개",
                THREAD_COUNT, result.success(), result.conflict(), result.deadlock(), reservedRows);

        assertThat(result.deadlock()).isGreaterThan(0);
    }

    @Test
    @DisplayName("FK 제약마저 없으면 같은 좌석이 중복 예매된다 (오버부킹)")
    void overbookingWithoutFk() throws Exception {
        // given — FK를 잠시 제거해 코드 자체에는 race condition에서의 정합성 방어가 전혀 없음을 드러내는 실험
        Long seatId = anyAvailableSeatId();
        jdbcTemplate.execute("ALTER TABLE reservation_seat DROP FOREIGN KEY fk_reservation_seat_schedule_seat");
        try {
            // when
            StormResult result = reserveConcurrently(seatId);

            // then — 한 좌석에 유효한 예약이 여러 건 = 오버부킹
            int reservedRows = reservationSeatRows(seatId);
            log.info("[재현] FK 제거 후 좌석 1개에 동시 예매 {}건 → 성공 {}건, 좌석충돌(409) {}건, 데드락 {}건, reservation_seat 행 {}개 (오버부킹)",
                    THREAD_COUNT, result.success(), result.conflict(), result.deadlock(), reservedRows);

            assertThat(result.success()).isGreaterThan(1);
            assertThat(reservedRows).isGreaterThan(1);
        } finally {
            jdbcTemplate.execute("""
                    ALTER TABLE reservation_seat
                    ADD CONSTRAINT fk_reservation_seat_schedule_seat
                    FOREIGN KEY (schedule_seat_id) REFERENCES schedule_seat (id)
                    """);
        }
    }

    private record StormResult(int success, int conflict, int deadlock) {
    }

    // 같은 좌석 하나에 THREAD_COUNT개의 스레드가 동시 예매를 한다.
    private StormResult reserveConcurrently(Long seatId) throws InterruptedException {
        Long memberId = seedMemberId();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        AtomicInteger deadlock = new AtomicInteger();

        boolean finished;
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await(); //스레드 전부 대기(동시성 극대화)
                        reservationService.reserve(memberId, new ReservationCreateRequest(List.of(seatId)));
                        success.incrementAndGet();
                    } catch (BusinessException e) {
                        conflict.incrementAndGet();
                    } catch (CannotAcquireLockException e) {
                        deadlock.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 동시 출발
            finished = doneLatch.await(30, TimeUnit.SECONDS);
        }
        assertThat(finished).isTrue();

        assertThat(success.get() + conflict.get() + deadlock.get()).isEqualTo(THREAD_COUNT);
        return new StormResult(success.get(), conflict.get(), deadlock.get());
    }

    private Long anyAvailableSeatId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule_seat WHERE status = 'AVAILABLE' ORDER BY id LIMIT 1", Long.class);
    }

    private Long seedMemberId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = 'user1@seatlock.com'", Long.class);
    }

    private int reservationSeatRows(Long seatId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_seat WHERE schedule_seat_id = ?", Integer.class, seatId);
    }
}
