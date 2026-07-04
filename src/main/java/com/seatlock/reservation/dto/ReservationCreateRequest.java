package com.seatlock.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReservationCreateRequest(
        @NotEmpty @Size(max = 10) List<Long> scheduleSeatIds
) {
}
