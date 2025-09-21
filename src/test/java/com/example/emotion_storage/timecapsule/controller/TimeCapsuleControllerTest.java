package com.example.emotion_storage.timecapsule.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.service.TimeCapsuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TimeCapsuleController.class)
@Import(TestSecurityConfig.class)
class TimeCapsuleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean TimeCapsuleService timeCapsuleService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 타임캡슐이_존재하는_날짜_리스트를_반환한다() throws Exception {
        // given
        LocalDate current = LocalDate.now();
        int year = current.getYear();
        int month = current.getMonthValue();

        TimeCapsuleExistDateResponse response =
                new TimeCapsuleExistDateResponse(2,
                        List.of(LocalDate.of(year, month, 2), LocalDate.of(year, month, 18))
                );

        given(timeCapsuleService.getActiveDatesForMonth(eq(year), eq(month), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/time-capsule/date")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.totalDates").value(2))
                .andExpect(jsonPath("$.data.dates[0]").value(LocalDate.of(year, month, 2).toString()))
                .andExpect(jsonPath("$.data.dates[1]").value(LocalDate.of(year, month, 18).toString()));
    }
}