-- 공연 도메인: 공연장 / 공연 / 회차

CREATE TABLE venue (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100)  NOT NULL,
    address VARCHAR(255)  NOT NULL
);

CREATE TABLE performance (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT       NOT NULL,
    title    VARCHAR(100) NOT NULL,
    genre    VARCHAR(50)  NOT NULL,
    CONSTRAINT fk_performance_venue FOREIGN KEY (venue_id) REFERENCES venue (id)
);

CREATE TABLE schedule (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    performance_id BIGINT   NOT NULL,
    start_at       DATETIME NOT NULL,
    CONSTRAINT fk_schedule_performance FOREIGN KEY (performance_id) REFERENCES performance (id)
);

-- 시드 데이터
INSERT INTO venue (name, address)
VALUES ('세종문화회관 대극장', '서울특별시 종로구 세종대로 175');

INSERT INTO performance (venue_id, title, genre)
VALUES (1, '시카고', '뮤지컬');

INSERT INTO schedule (performance_id, start_at)
VALUES (1, '2026-08-15 19:00:00'),
       (1, '2026-08-16 19:00:00');
