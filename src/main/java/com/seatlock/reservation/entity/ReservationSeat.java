package com.seatlock.reservation.entity;

import com.seatlock.seat.entity.ScheduleSeat;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_seat_id")
    private ScheduleSeat scheduleSeat;

    public static ReservationSeat of(Reservation reservation, ScheduleSeat scheduleSeat) {
        ReservationSeat reservationSeat = new ReservationSeat();
        reservationSeat.reservation = reservation;
        reservationSeat.scheduleSeat = scheduleSeat;
        return reservationSeat;
    }
}
