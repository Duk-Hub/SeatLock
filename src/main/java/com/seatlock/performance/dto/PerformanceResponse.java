package com.seatlock.performance.dto;

import com.seatlock.performance.entity.Performance;

public record PerformanceResponse(
        Long id,
        String title,
        String genre,
        String venueName
) {
    public static PerformanceResponse from(Performance performance) {
        return new PerformanceResponse(
                performance.getId(),
                performance.getTitle(),
                performance.getGenre(),
                performance.getVenue().getName()
        );
    }
}
