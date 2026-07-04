package com.seatlock.reservation.controller;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.reservation.dto.ReservationCreateRequest;
import com.seatlock.reservation.dto.ReservationResponse;
import com.seatlock.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "예매", description = "좌석 예매")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "좌석 예매", description = "같은 회차의 좌석 최대 10석을 한 번에 예매한다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> reserve(@AuthenticationPrincipal Long memberId,
                                                    @RequestBody @Valid ReservationCreateRequest request) {
        return ApiResponse.ok(reservationService.reserve(memberId, request));
    }
}
