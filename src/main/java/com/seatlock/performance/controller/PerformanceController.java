package com.seatlock.performance.controller;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.performance.dto.PerformanceResponse;
import com.seatlock.performance.dto.ScheduleResponse;
import com.seatlock.performance.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공연", description = "공연 및 회차 조회")
@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @Operation(summary = "공연 목록 조회")
    @GetMapping
    public ApiResponse<List<PerformanceResponse>> getPerformances() {
        return ApiResponse.ok(performanceService.findPerformances());
    }

    @Operation(summary = "공연 회차 목록 조회")
    @GetMapping("/{performanceId}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedules(@PathVariable Long performanceId) {
        return ApiResponse.ok(performanceService.findSchedules(performanceId));
    }
}
