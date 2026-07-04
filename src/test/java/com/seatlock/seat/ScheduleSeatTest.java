package com.seatlock.seat;

import com.seatlock.global.exception.BusinessException;
import com.seatlock.global.exception.ErrorCode;
import com.seatlock.seat.entity.ScheduleSeat;
import com.seatlock.seat.entity.ScheduleSeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleSeatTest {

    @Test
    @DisplayName("AVAILABLE 좌석을 예매하면 RESERVED로 전이한다")
    void reserveAvailableSeat() {
        // given
        ScheduleSeat scheduleSeat = seatWithStatus(ScheduleSeatStatus.AVAILABLE);

        // when
        scheduleSeat.reserve();

        // then
        assertThat(scheduleSeat.getStatus()).isEqualTo(ScheduleSeatStatus.RESERVED);
    }

    @Test
    @DisplayName("이미 RESERVED인 좌석을 예매하면 SEAT_ALREADY_RESERVED 예외가 발생한다")
    void reserveReservedSeat() {
        // given
        ScheduleSeat scheduleSeat = seatWithStatus(ScheduleSeatStatus.RESERVED);

        // when & then
        assertThatThrownBy(scheduleSeat::reserve)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
    }

    @Test
    @DisplayName("PENDING 좌석도 예매할 수 없다 (AVAILABLE만 예매 가능)")
    void reservePendingSeat() {
        // given
        ScheduleSeat scheduleSeat = seatWithStatus(ScheduleSeatStatus.PENDING);

        // when & then
        assertThatThrownBy(scheduleSeat::reserve)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
    }

    private ScheduleSeat seatWithStatus(ScheduleSeatStatus status) {
        ScheduleSeat scheduleSeat = BeanUtils.instantiateClass(ScheduleSeat.class);
        ReflectionTestUtils.setField(scheduleSeat, "status", status);
        return scheduleSeat;
    }
}
