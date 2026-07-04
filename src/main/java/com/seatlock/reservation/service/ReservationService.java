package com.seatlock.reservation.service;

import com.seatlock.global.exception.BusinessException;
import com.seatlock.global.exception.ErrorCode;
import com.seatlock.member.entity.Member;
import com.seatlock.member.repository.MemberRepository;
import com.seatlock.reservation.dto.ReservationCreateRequest;
import com.seatlock.reservation.dto.ReservationResponse;
import com.seatlock.reservation.entity.Reservation;
import com.seatlock.reservation.entity.ReservationSeat;
import com.seatlock.reservation.repository.ReservationRepository;
import com.seatlock.reservation.repository.ReservationSeatRepository;
import com.seatlock.seat.entity.ScheduleSeat;
import com.seatlock.seat.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationResponse reserve(Long memberId, ReservationCreateRequest request) {
        List<Long> scheduleSeatIds = request.scheduleSeatIds();
        validateNoDuplicate(scheduleSeatIds);

        log.info("예매 시도(락 대기 진입): memberId={} scheduleSeatIds={}", memberId, scheduleSeatIds);
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findAllByIdForUpdate(scheduleSeatIds);
        log.info("좌석 락 획득: memberId={} scheduleSeatIds={}", memberId, scheduleSeatIds);
        validateAllExist(scheduleSeatIds, scheduleSeats);
        validateSameSchedule(scheduleSeats);

        scheduleSeats.forEach(ScheduleSeat::reserve);

        Member member = memberRepository.getReferenceById(memberId);
        Reservation reservation = reservationRepository.save(Reservation.confirmed(member));
        reservationSeatRepository.saveAll(scheduleSeats.stream()
                .map(scheduleSeat -> ReservationSeat.of(reservation, scheduleSeat))
                .toList());

        log.info("예매 완료: reservationId={} memberId={} scheduleSeatIds={}",
                reservation.getId(), memberId, scheduleSeatIds);
        return ReservationResponse.of(reservation, scheduleSeats);
    }

    private void validateNoDuplicate(List<Long> scheduleSeatIds) {
        if (new HashSet<>(scheduleSeatIds).size() != scheduleSeatIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateAllExist(List<Long> scheduleSeatIds, List<ScheduleSeat> scheduleSeats) {
        if (scheduleSeats.size() != scheduleSeatIds.size()) {
            throw new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
        }
    }

    private void validateSameSchedule(List<ScheduleSeat> scheduleSeats) {
        long distinctScheduleCount = scheduleSeats.stream()
                .map(scheduleSeat -> scheduleSeat.getSchedule().getId())
                .distinct()
                .count();
        if (distinctScheduleCount != 1) {
            throw new BusinessException(ErrorCode.SCHEDULE_MISMATCH);
        }
    }
}
