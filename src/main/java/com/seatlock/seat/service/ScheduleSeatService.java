package com.seatlock.seat.service;

import com.seatlock.global.exception.BusinessException;
import com.seatlock.global.exception.ErrorCode;
import com.seatlock.performance.repository.ScheduleRepository;
import com.seatlock.seat.dto.ScheduleSeatResponse;
import com.seatlock.seat.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleSeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    public List<ScheduleSeatResponse> findSeats(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        return scheduleSeatRepository.findAllByScheduleIdWithSeat(scheduleId).stream()
                .map(ScheduleSeatResponse::from)
                .toList();
    }
}
