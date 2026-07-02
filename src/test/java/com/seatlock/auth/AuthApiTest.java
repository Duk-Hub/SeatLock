package com.seatlock.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Flyway 시드(V3: user1~3@seatlock.com / password123!) 기반 인증 통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("시드 회원으로 로그인하면 액세스 토큰을 발급한다")
    void login() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user1@seatlock.com\",\"password\":\"password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401 INVALID_CREDENTIALS를 반환한다")
    void loginWithWrongPassword() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user1@seatlock.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일도 같은 401 INVALID_CREDENTIALS를 반환한다")
    void loginWithUnknownEmail() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@seatlock.com\",\"password\":\"password123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("토큰 없이 보호 자원에 접근하면 401 공통 포맷으로 응답한다")
    void protectedResourceWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.traceId").exists());
    }

    @Test
    @DisplayName("무효 토큰을 제시하면 공개 자원이라도 필터가 즉시 401로 응답한다")
    void invalidToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/performances").header(AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("유효한 토큰이면 시큐리티를 통과한다 (미존재 경로라 404 — 401이 아님)")
    void protectedResourceWithToken() throws Exception {
        // given — 로그인으로 실제 토큰 확보
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user1@seatlock.com\",\"password\":\"password123!\"}"))
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asString();

        // when & then
        mockMvc.perform(post("/api/protected").header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
