package com.seatlock.seat.controller;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.seat.dto.ScheduleSeatResponse;
import com.seatlock.seat.service.ScheduleSeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "좌석", description = "회차별 좌석 조회")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    @Operation(summary = "회차별 좌석 목록 조회")
    @GetMapping("/{scheduleId}/seats")
    public ApiResponse<List<ScheduleSeatResponse>> getSeats(@PathVariable Long scheduleId) {
        return ApiResponse.ok(scheduleSeatService.findSeats(scheduleId));
    }
}
