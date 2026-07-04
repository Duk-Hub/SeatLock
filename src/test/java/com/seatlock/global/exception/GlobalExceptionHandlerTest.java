package com.seatlock.global.exception;

import com.seatlock.global.security.JwtAuthenticationEntryPoint;
import com.seatlock.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 핸들러 계약(예외 → 공통 응답 변환)만 검증 — 필터(Security·MDC Logging)는 제외. traceId는 통합 테스트에서 검증
@WebMvcTest(controllers = ExceptionTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    // @WebMvcTest가 Filter 빈(JwtAuthenticationFilter)을 포함해서 그 의존성만 mock으로 충족
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태코드와 공통 에러 포맷으로 변환된다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("검증 실패는 400과 필드 에러 목록을 반환한다")
    void validationFailure() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("name"));
    }

    @Test
    @DisplayName("존재하지 않는 경로는 404 공통 포맷으로 응답한다")
    void unknownPath() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("비관적 락 실패(데드락·락 타임아웃)는 409 공통 포맷으로 변환된다")
    void pessimisticLockFailure() throws Exception {
        mockMvc.perform(get("/test/lock-failure"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LOCK_TIMEOUT"))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.LOCK_TIMEOUT.getMessage()));
    }

    @Test
    @DisplayName("예상치 못한 예외는 500과 사전 정의 메시지만 노출한다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
