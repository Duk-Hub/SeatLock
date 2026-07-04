package com.seatlock.reservation.dto;

import com.seatlock.reservation.entity.Reservation;
import com.seatlock.reservation.entity.ReservationStatus;
import com.seatlock.seat.entity.ScheduleSeat;

import java.util.List;

public record ReservationResponse(
        Long reservationId,
        ReservationStatus status,
        List<Long> scheduleSeatIds,
        int totalPrice
) {
    public static ReservationResponse of(Reservation reservation, List<ScheduleSeat> scheduleSeats) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus(),
                scheduleSeats.stream().map(ScheduleSeat::getId).toList(),
                scheduleSeats.stream().mapToInt(ScheduleSeat::getPrice).sum()
        );
    }
}
