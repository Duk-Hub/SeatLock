package com.seatlock.seat.repository;

import com.seatlock.seat.entity.ScheduleSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    @Query("""
            select ss
            from ScheduleSeat ss
            join fetch ss.seat s
            where ss.schedule.id = :scheduleId
            order by s.section, s.rowNo, s.seatNo
            """)
    List<ScheduleSeat> findAllByScheduleIdWithSeat(@Param("scheduleId") Long scheduleId);
}
