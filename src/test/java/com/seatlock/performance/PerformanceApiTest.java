package com.seatlock.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PerformanceApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("공연 목록을 공연장 이름과 함께 반환한다")
    void getPerformances() throws Exception {
        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("시카고"))
                .andExpect(jsonPath("$.data[0].venueName").value("세종문화회관 대극장"));
    }

    @Test
    @DisplayName("공연의 회차 목록을 반환한다")
    void getSchedules() throws Exception {
        mockMvc.perform(get("/api/performances/1/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].startAt").exists());
    }

    @Test
    @DisplayName("존재하지 않는 공연의 회차 조회는 404를 반환한다")
    void getNonExistentPerformanceSchedules() throws Exception {
        mockMvc.perform(get("/api/performances/999/schedules"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PERFORMANCE_NOT_FOUND"));
    }
}
