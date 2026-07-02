package com.seatlock.performance.controller;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.performance.dto.PerformanceResponse;
import com.seatlock.performance.dto.ScheduleResponse;
import com.seatlock.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping
    public ApiResponse<List<PerformanceResponse>> getPerformances() {
        return ApiResponse.ok(performanceService.findPerformances());
    }

    @GetMapping("/{performanceId}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedules(@PathVariable Long performanceId) {
        return ApiResponse.ok(performanceService.findSchedules(performanceId));
    }
}
