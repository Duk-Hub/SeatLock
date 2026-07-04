package com.seatlock.seat.repository;

import com.seatlock.seat.entity.ScheduleSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ScheduleSeat ss where ss.id in :ids")
    List<ScheduleSeat> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    @Query("""
            select ss
            from ScheduleSeat ss
            join fetch ss.seat s
            where ss.schedule.id = :scheduleId
            order by s.section, s.rowNo, s.seatNo
            """)
    List<ScheduleSeat> findAllByScheduleIdWithSeat(@Param("scheduleId") Long scheduleId);
}
