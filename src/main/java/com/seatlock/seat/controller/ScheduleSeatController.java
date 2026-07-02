package com.seatlock.seat.controller;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.seat.dto.ScheduleSeatResponse;
import com.seatlock.seat.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    @GetMapping("/{scheduleId}/seats")
    public ApiResponse<List<ScheduleSeatResponse>> getSeats(@PathVariable Long scheduleId) {
        return ApiResponse.ok(scheduleSeatService.findSeats(scheduleId));
    }
}
