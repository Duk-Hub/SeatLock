package com.seatlock.performance.dto;

import com.seatlock.performance.entity.Schedule;

import java.time.LocalDateTime;

public record ScheduleResponse(
        Long id,
        LocalDateTime startAt
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(), schedule.getStartAt());
    }
}
