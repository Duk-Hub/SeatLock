package com.seatlock.seat.dto;

import com.seatlock.seat.entity.ScheduleSeat;
import com.seatlock.seat.entity.ScheduleSeatStatus;

public record ScheduleSeatResponse(
        Long scheduleSeatId,
        String section,
        int rowNo,
        int seatNo,
        String grade,
        int price,
        ScheduleSeatStatus status
) {
    public static ScheduleSeatResponse from(ScheduleSeat scheduleSeat) {
        return new ScheduleSeatResponse(
                scheduleSeat.getId(),
                scheduleSeat.getSeat().getSection(),
                scheduleSeat.getSeat().getRowNo(),
                scheduleSeat.getSeat().getSeatNo(),
                scheduleSeat.getSeat().getGrade(),
                scheduleSeat.getPrice(),
                scheduleSeat.getStatus()
        );
    }
}
