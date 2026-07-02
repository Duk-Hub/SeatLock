package com.seatlock.performance.repository;

import com.seatlock.performance.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByPerformanceId(Long performanceId);
}
