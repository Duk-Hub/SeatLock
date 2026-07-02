-- 좌석 / 회차별 좌석

CREATE TABLE seat (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id    BIGINT      NOT NULL,
    section     VARCHAR(10) NOT NULL,
    row_no      INT         NOT NULL,
    seat_no     INT         NOT NULL,
    grade       VARCHAR(10) NOT NULL,
    CONSTRAINT fk_seat_venue FOREIGN KEY (venue_id) REFERENCES venue (id)
);

CREATE TABLE schedule_seat (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT      NOT NULL,
    seat_id     BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,
    price       INT         NOT NULL,
    CONSTRAINT fk_schedule_seat_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_schedule_seat_seat FOREIGN KEY (seat_id) REFERENCES seat (id),
    CONSTRAINT uk_schedule_seat UNIQUE (schedule_id, seat_id)
);

-- 좌석 시드: A/B 구역 × 10열 × 10석 = 200석 (1~3열 VIP, 4~6열 R, 7~10열 S)
INSERT INTO seat (venue_id, section, row_no, seat_no, grade)
WITH RECURSIVE nums AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM nums WHERE n < 10)
SELECT 1,
       sec.name,
       r.n,
       s.n,
       CASE WHEN r.n <= 3 THEN 'VIP' WHEN r.n <= 6 THEN 'R' ELSE 'S' END
FROM (SELECT 'A' AS name UNION ALL SELECT 'B') sec,
     nums r,
     nums s;

-- 회차 × 좌석 크로스 조인 = 400행
INSERT INTO schedule_seat (schedule_id, seat_id, status, price)
SELECT sch.id,
       st.id,
       'AVAILABLE',
       CASE st.grade WHEN 'VIP' THEN 150000 WHEN 'R' THEN 120000 ELSE 90000 END
FROM schedule sch
         CROSS JOIN seat st;
