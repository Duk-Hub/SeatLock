package com.seatlock.reservation;

import com.seatlock.IntegrationTest;
import com.seatlock.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ReservationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    // 이전 테스트가 남긴 상태 정리 — FK 역순 삭제 + 시드 상태 복원
    @BeforeEach
    void cleanBefore() {
        jdbcTemplate.update("DELETE FROM reservation_seat");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("UPDATE schedule_seat SET status = 'AVAILABLE'");
    }

    @Test
    @DisplayName("같은 회차의 좌석 2석을 예매하면 201과 함께 좌석이 RESERVED로 저장된다")
    void reserve() throws Exception {
        // given — 전제(같은 회차·AVAILABLE)는 쿼리로 확보
        List<Long> seatIds = availableSeatIds(1L, 2);
        Long firstSeatId = seatIds.get(0);
        Long secondSeatId = seatIds.get(1);
        int expectedTotalPrice = jdbcTemplate.queryForObject(
                "SELECT SUM(price) FROM schedule_seat WHERE id IN (?, ?)",
                Integer.class, firstSeatId, secondSeatId);

        // when & then
        mockMvc.perform(post("/api/reservations")
                        .header(AUTHORIZATION, "Bearer " + seedMemberToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleSeatIds\":[" + firstSeatId + "," + secondSeatId + "]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reservationId").exists())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.scheduleSeatIds.length()").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(expectedTotalPrice));

        assertThat(seatStatuses(seatIds)).containsOnly("RESERVED");
        assertThat(count("reservation")).isEqualTo(1);
        assertThat(count("reservation_seat")).isEqualTo(2);
    }

    @Test
    @DisplayName("토큰 없이 예매를 요청하면 401을 반환한다")
    void reserveWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleSeatIds\":[1]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("N석 중 한 석이라도 이미 예매돼 있으면 409와 함께 전체가 롤백된다")
    void reserveAlreadyReservedSeatRollsBackAll() throws Exception {
        // given — 두 좌석 중 하나를 미리 RESERVED로
        List<Long> seatIds = availableSeatIds(1L, 2);
        Long availableSeatId = seatIds.get(0);
        Long reservedSeatId = seatIds.get(1);
        jdbcTemplate.update("UPDATE schedule_seat SET status = 'RESERVED' WHERE id = ?", reservedSeatId);

        // when & then
        mockMvc.perform(post("/api/reservations")
                        .header(AUTHORIZATION, "Bearer " + seedMemberToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleSeatIds\":[" + availableSeatId + "," + reservedSeatId + "]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SEAT_ALREADY_RESERVED"));

        // 부분 실패 = 전체 롤백: 나머지 좌석은 AVAILABLE 그대로, 예약도 저장되지 않음
        assertThat(seatStatuses(List.of(availableSeatId))).containsOnly("AVAILABLE");
        assertThat(count("reservation")).isZero();
        assertThat(count("reservation_seat")).isZero();
    }

    @Test
    @DisplayName("다른 회차의 좌석을 섞어 요청하면 400 SCHEDULE_MISMATCH를 반환한다")
    void reserveSeatsFromDifferentSchedules() throws Exception {
        // given — 회차 1·2에서 좌석을 하나씩
        Long seatOfSchedule1 = availableSeatIds(1L, 1).getFirst();
        Long seatOfSchedule2 = availableSeatIds(2L, 1).getFirst();

        // when & then
        mockMvc.perform(post("/api/reservations")
                        .header(AUTHORIZATION, "Bearer " + seedMemberToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleSeatIds\":[" + seatOfSchedule1 + "," + seatOfSchedule2 + "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SCHEDULE_MISMATCH"));
    }

    @Test
    @DisplayName("11석 이상을 요청하면 400 INVALID_INPUT을 반환한다 (최대 10석)")
    void reserveMoreThanMaxSeats() throws Exception {
        // given — 1~11 좌석 ID (검증 어노테이션에서 걸리므로 존재 여부는 무관)
        String elevenIds = "[1,2,3,4,5,6,7,8,9,10,11]";

        // when & then
        mockMvc.perform(post("/api/reservations")
                        .header(AUTHORIZATION, "Bearer " + seedMemberToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleSeatIds\":" + elevenIds + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    //로그인 API 대신 토큰을 직접 발급한다
    private String seedMemberToken() {
        Long memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = 'user1@seatlock.com'", Long.class);
        return jwtTokenProvider.createToken(memberId);
    }

    private List<Long> availableSeatIds(Long scheduleId, int count) {
        return jdbcTemplate.queryForList(
                "SELECT id FROM schedule_seat WHERE schedule_id = ? AND status = 'AVAILABLE' ORDER BY id LIMIT " + count,
                Long.class, scheduleId);
    }

    private List<String> seatStatuses(List<Long> seatIds) {
        return seatIds.stream()
                .map(id -> jdbcTemplate.queryForObject("SELECT status FROM schedule_seat WHERE id = ?", String.class, id))
                .toList();
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
