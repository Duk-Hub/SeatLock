package com.seatlock.reservation;

import com.seatlock.global.exception.BusinessException;
import com.seatlock.global.exception.ErrorCode;
import com.seatlock.member.entity.Member;
import com.seatlock.member.repository.MemberRepository;
import com.seatlock.performance.entity.Schedule;
import com.seatlock.reservation.dto.ReservationCreateRequest;
import com.seatlock.reservation.dto.ReservationResponse;
import com.seatlock.reservation.entity.Reservation;
import com.seatlock.reservation.entity.ReservationStatus;
import com.seatlock.reservation.repository.ReservationRepository;
import com.seatlock.reservation.repository.ReservationSeatRepository;
import com.seatlock.reservation.service.ReservationService;
import com.seatlock.seat.entity.ScheduleSeat;
import com.seatlock.seat.entity.ScheduleSeatStatus;
import com.seatlock.seat.repository.ScheduleSeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ScheduleSeatRepository scheduleSeatRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationSeatRepository reservationSeatRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    ReservationService reservationService;

    private static final Long MEMBER_ID = 1L;

    @Test
    @DisplayName("같은 회차의 AVAILABLE 좌석 N석을 예매하면 모두 RESERVED가 되고 CONFIRMED 예약이 저장된다")
    void reserveMultipleSeats() {
        // given
        Schedule schedule = scheduleWithId(1L);
        ScheduleSeat seat1 = scheduleSeat(11L, schedule, ScheduleSeatStatus.AVAILABLE, 150000);
        ScheduleSeat seat2 = scheduleSeat(12L, schedule, ScheduleSeatStatus.AVAILABLE, 120000);
        given(scheduleSeatRepository.findAllByIdForUpdate(List.of(11L, 12L))).willReturn(List.of(seat1, seat2));
        given(memberRepository.getReferenceById(MEMBER_ID)).willReturn(mock(Member.class));
        given(reservationRepository.save(any(Reservation.class))).willAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            ReflectionTestUtils.setField(reservation, "id", 100L);
            return reservation;
        });

        // when
        ReservationResponse response = reservationService.reserve(MEMBER_ID, new ReservationCreateRequest(List.of(11L, 12L)));

        // then
        assertThat(seat1.getStatus()).isEqualTo(ScheduleSeatStatus.RESERVED);
        assertThat(seat2.getStatus()).isEqualTo(ScheduleSeatStatus.RESERVED);
        assertThat(response.reservationId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.scheduleSeatIds()).containsExactly(11L, 12L);
        assertThat(response.totalPrice()).isEqualTo(270000);
        verify(reservationSeatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("중복된 좌석 ID가 있으면 INVALID_INPUT — 좌석 조회 전에 걸러진다")
    void duplicateSeatIds() {
        // when & then
        assertThatThrownBy(() -> reservationService.reserve(MEMBER_ID, new ReservationCreateRequest(List.of(11L, 11L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        verify(scheduleSeatRepository, never()).findAllByIdForUpdate(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 좌석 ID가 섞여 있으면 SCHEDULE_SEAT_NOT_FOUND")
    void seatNotFound() {
        // given — 2개를 요청했지만 1개만 조회됨
        Schedule schedule = scheduleWithId(1L);
        given(scheduleSeatRepository.findAllByIdForUpdate(List.of(11L, 999L)))
                .willReturn(List.of(scheduleSeat(11L, schedule, ScheduleSeatStatus.AVAILABLE, 150000)));

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(MEMBER_ID, new ReservationCreateRequest(List.of(11L, 999L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 회차의 좌석이 섞여 있으면 SCHEDULE_MISMATCH")
    void scheduleMismatch() {
        // given
        ScheduleSeat seat1 = scheduleSeat(11L, scheduleWithId(1L), ScheduleSeatStatus.AVAILABLE, 150000);
        ScheduleSeat seat2 = scheduleSeat(12L, scheduleWithId(2L), ScheduleSeatStatus.AVAILABLE, 150000);
        given(scheduleSeatRepository.findAllByIdForUpdate(List.of(11L, 12L))).willReturn(List.of(seat1, seat2));

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(MEMBER_ID, new ReservationCreateRequest(List.of(11L, 12L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SCHEDULE_MISMATCH);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 예매된 좌석이 섞여 있으면 SEAT_ALREADY_RESERVED — 예약은 저장되지 않는다")
    void seatAlreadyReserved() {
        // given
        Schedule schedule = scheduleWithId(1L);
        ScheduleSeat available = scheduleSeat(11L, schedule, ScheduleSeatStatus.AVAILABLE, 150000);
        ScheduleSeat reserved = scheduleSeat(12L, schedule, ScheduleSeatStatus.RESERVED, 150000);
        given(scheduleSeatRepository.findAllByIdForUpdate(List.of(11L, 12L))).willReturn(List.of(available, reserved));

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(MEMBER_ID, new ReservationCreateRequest(List.of(11L, 12L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
        verify(reservationRepository, never()).save(any());
        verify(reservationSeatRepository, never()).saveAll(anyList());
    }

    private Schedule scheduleWithId(Long id) {
        Schedule schedule = BeanUtils.instantiateClass(Schedule.class);
        ReflectionTestUtils.setField(schedule, "id", id);
        return schedule;
    }

    private ScheduleSeat scheduleSeat(Long id, Schedule schedule, ScheduleSeatStatus status, int price) {
        ScheduleSeat scheduleSeat = BeanUtils.instantiateClass(ScheduleSeat.class);
        ReflectionTestUtils.setField(scheduleSeat, "id", id);
        ReflectionTestUtils.setField(scheduleSeat, "schedule", schedule);
        ReflectionTestUtils.setField(scheduleSeat, "status", status);
        ReflectionTestUtils.setField(scheduleSeat, "price", price);
        return scheduleSeat;
    }
}
