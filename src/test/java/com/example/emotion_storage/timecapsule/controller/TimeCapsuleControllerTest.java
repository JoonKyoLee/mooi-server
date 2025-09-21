package com.example.emotion_storage.timecapsule.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.service.TimeCapsuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void 특정_날짜에_존재하는_타임캡슐_리스트를_반환한다() throws Exception {
        // given
        LocalDate targetDate = LocalDate.now();
        int page = 1;
        int limit = 15;

        TimeCapsuleListResponse response =
                new TimeCapsuleListResponse(
                        new PaginationDto(
                                1, 15, 1
                        ),
                        2,
                        List.of(
                                new TimeCapsuleDto(
                                        1L,
                                        LocalDateTime.now(),
                                        LocalDateTime.now(),
                                        LocalDateTime.now(),
                                        null,
                                        false,
                                        List.of("행복", "즐거움"),
                                        "오늘 아침에 ...",
                                        "임시 저장"
                                ),
                                new TimeCapsuleDto(
                                        2L,
                                        LocalDateTime.now(),
                                        LocalDateTime.now(),
                                        LocalDateTime.now(),
                                        LocalDateTime.now().plusDays(1),
                                        true,
                                        List.of("고마움", "안정감"),
                                        "오늘 점심에 ...",
                                        "잠김"
                                )
                        )
                );

        given(timeCapsuleService.fetchTimeCapsules(
                eq(targetDate), eq(targetDate), eq(page), eq(limit), eq("all"), anyLong()
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/time-capsule")
                        .param("startDate", targetDate.toString())
                        .param("endDate", targetDate.toString())
                        .param("page", String.valueOf(page))
                        .param("limit", String.valueOf(limit))
                        .param("status", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage()))
                // 페이징
                .andExpect(jsonPath("$.data.pagination.page").value(page))
                .andExpect(jsonPath("$.data.pagination.limit").value(limit))
                .andExpect(jsonPath("$.data.pagination.totalPage").value(1))
                // 총 개수/목록 크기
                .andExpect(jsonPath("$.data.totalCapsules").value(2))
                .andExpect(jsonPath("$.data.timeCapsules.length()").value(2))
                // 임시 저장
                .andExpect(jsonPath("$.data.timeCapsules[0].status").value("임시 저장"))
                .andExpect(jsonPath("$.data.timeCapsules[0].openAt", nullValue()))
                .andExpect(jsonPath("$.data.timeCapsules[0].title").value("오늘 아침에 ..."))
                // 잠김
                .andExpect(jsonPath("$.data.timeCapsules[1].status").value("잠김"))
                .andExpect(jsonPath("$.data.timeCapsules[1].openAt", notNullValue()))
                .andExpect(jsonPath("$.data.timeCapsules[1].title").value("오늘 점심에 ..."));
    }

    @Test
    void 도착한_타임캡슐_리스트를_반환한다() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().minusDays(21);
        LocalDate endDate = LocalDate.now();
        int page = 1;
        int limit = 15;

        TimeCapsuleListResponse response =
                new TimeCapsuleListResponse(
                        new PaginationDto(
                                1, 15, 1
                        ),
                        2,
                        List.of(
                                new TimeCapsuleDto(
                                        2L,
                                        LocalDateTime.now().minusDays(5),
                                        LocalDateTime.now().minusDays(5),
                                        LocalDateTime.now().minusDays(5),
                                        LocalDateTime.now().minusDays(3),
                                        true,
                                        List.of("고마움", "안정감"),
                                        "오늘 점심에 ...",
                                        "도착"
                                ),
                                new TimeCapsuleDto(
                                        1L,
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(7),
                                        false,
                                        List.of("행복", "즐거움"),
                                        "오늘 아침에 ...",
                                        "도착"
                                )
                        )
                );

        given(timeCapsuleService.fetchTimeCapsules(
                eq(startDate), eq(endDate), eq(page), eq(limit), eq("all"), anyLong()
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/time-capsule")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", String.valueOf(page))
                        .param("limit", String.valueOf(limit))
                        .param("status", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage()))
                // 페이징
                .andExpect(jsonPath("$.data.pagination.page").value(page))
                .andExpect(jsonPath("$.data.pagination.limit").value(limit))
                .andExpect(jsonPath("$.data.pagination.totalPage").value(1))
                // 총 개수/목록 크기
                .andExpect(jsonPath("$.data.totalCapsules").value(2))
                .andExpect(jsonPath("$.data.timeCapsules.length()").value(2))
                // 도착한 타임캡슐
                .andExpect(jsonPath("$.data.timeCapsules[0].id").value(2L))
                .andExpect(jsonPath("$.data.timeCapsules[0].status").value("도착"))
                .andExpect(jsonPath("$.data.timeCapsules[0].openAt", notNullValue()))
                .andExpect(jsonPath("$.data.timeCapsules[0].title").value("오늘 점심에 ..."))
                .andExpect(jsonPath("$.data.timeCapsules[1].id").value(1L))
                .andExpect(jsonPath("$.data.timeCapsules[1].status").value("도착"))
                .andExpect(jsonPath("$.data.timeCapsules[1].openAt", notNullValue()))
                .andExpect(jsonPath("$.data.timeCapsules[1].title").value("오늘 아침에 ..."));
    }
}