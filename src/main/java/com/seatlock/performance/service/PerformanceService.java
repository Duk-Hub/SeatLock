package com.seatlock.performance.service;

import com.seatlock.global.exception.BusinessException;
import com.seatlock.global.exception.ErrorCode;
import com.seatlock.performance.dto.PerformanceResponse;
import com.seatlock.performance.dto.ScheduleResponse;
import com.seatlock.performance.repository.PerformanceRepository;
import com.seatlock.performance.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final ScheduleRepository scheduleRepository;

    public List<PerformanceResponse> findPerformances() {
        return performanceRepository.findAllWithVenue().stream()
                .map(PerformanceResponse::from)
                .toList();
    }

    public List<ScheduleResponse> findSchedules(Long performanceId) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND);
        }
        return scheduleRepository.findAllByPerformanceId(performanceId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }
}
