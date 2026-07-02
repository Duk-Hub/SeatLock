package com.seatlock.performance.repository;

import com.seatlock.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    @Query("select p from Performance p join fetch p.venue")
    List<Performance> findAllWithVenue();
}
