package com.seatlock.seat;

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
class ScheduleSeatApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("한 회차의 좌석 목록 200석을 좌석 정보·가격·상태와 함께 반환한다")
    void getSeats() throws Exception {
        mockMvc.perform(get("/api/schedules/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(200))
                .andExpect(jsonPath("$.data[0].section").value("A"))
                .andExpect(jsonPath("$.data[0].grade").value("VIP"))
                .andExpect(jsonPath("$.data[0].price").value(150000))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("존재하지 않는 회차의 좌석 조회는 404를 반환한다")
    void getSeatsOfUnknownSchedule() throws Exception {
        mockMvc.perform(get("/api/schedules/999/seats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCHEDULE_NOT_FOUND"));
    }
}
