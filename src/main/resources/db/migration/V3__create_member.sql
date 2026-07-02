-- 회원 (회원가입 API 없음 — 시드로만 주입)

CREATE TABLE member (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(100) NOT NULL,
    password VARCHAR(60)  NOT NULL,
    name     VARCHAR(50)  NOT NULL,
    CONSTRAINT uk_member_email UNIQUE (email)
);

-- 시드 회원 3명 (비밀번호: password123!)
INSERT INTO member (email, password, name)
VALUES ('user1@seatlock.com', '$2a$10$/eBlnyHU5jUO2Tslse/J4.cVLY4DxmGlNFgpWZHM8A5owZVre/7MO', '테스터1'),
       ('user2@seatlock.com', '$2a$10$/eBlnyHU5jUO2Tslse/J4.cVLY4DxmGlNFgpWZHM8A5owZVre/7MO', '테스터2'),
       ('user3@seatlock.com', '$2a$10$/eBlnyHU5jUO2Tslse/J4.cVLY4DxmGlNFgpWZHM8A5owZVre/7MO', '테스터3');
