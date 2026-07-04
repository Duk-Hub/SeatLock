-- 예매 / 예매-좌석 연결

CREATE TABLE reservation (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    expires_at DATETIME    NULL,
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE reservation_seat (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id   BIGINT NOT NULL,
    schedule_seat_id BIGINT NOT NULL,
    CONSTRAINT fk_reservation_seat_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT fk_reservation_seat_schedule_seat FOREIGN KEY (schedule_seat_id) REFERENCES schedule_seat (id)
);
