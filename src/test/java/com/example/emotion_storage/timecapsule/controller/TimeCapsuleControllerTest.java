package com.example.emotion_storage.timecapsule.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.timecapsule.dto.EmotionDetailDto;
import com.example.emotion_storage.global.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleFavoriteRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleNoteUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleDetailResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleFavoriteResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Test
    void 최신_순으로_즐겨찾기_타임캡슐_리스트를_반환한다() throws Exception {
        // given
        int page = 1;
        int limit = 15;
        String sort = "all";

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
                                        true,
                                        List.of("행복", "즐거움"),
                                        "오늘 아침에 ...",
                                        "도착"
                                )
                        )
                );

        given(timeCapsuleService.fetchFavoriteTimeCapsules(
                eq(page), eq(limit), eq(sort), anyLong()
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/time-capsule/favorites")
                        .param("page", String.valueOf(page))
                        .param("limit", String.valueOf(limit))
                        .param("sort", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_FAVORITE_TIME_CAPSULE_LIST_SUCCESS.getMessage()))
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

    @Test
    void 즐겨찾기한_순으로_즐겨찾기_타임캡슐_리스트를_반환한다() throws Exception {
        // given
        int page = 1;
        int limit = 15;
        String sort = "favorite";

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
                                        "나중에 즐겨찾기한 타임캡슐",
                                        "도착"
                                ),
                                new TimeCapsuleDto(
                                        1L,
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(10),
                                        LocalDateTime.now().minusDays(7),
                                        true,
                                        List.of("행복", "즐거움"),
                                        "먼저 즐겨찾기한 타임캡슐",
                                        "도착"
                                )
                        )
                );

        given(timeCapsuleService.fetchFavoriteTimeCapsules(
                eq(page), eq(limit), eq(sort), anyLong()
        )).willReturn(response);


        // when & then
        mockMvc.perform(get("/api/v1/time-capsule/favorites")
                        .param("page", String.valueOf(page))
                        .param("limit", String.valueOf(limit))
                        .param("sort", "favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_FAVORITE_TIME_CAPSULE_LIST_SUCCESS.getMessage()))
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
                .andExpect(jsonPath("$.data.timeCapsules[0].title").value("나중에 즐겨찾기한 타임캡슐"))
                .andExpect(jsonPath("$.data.timeCapsules[1].id").value(1L))
                .andExpect(jsonPath("$.data.timeCapsules[1].status").value("도착"))
                .andExpect(jsonPath("$.data.timeCapsules[1].openAt", notNullValue()))
                .andExpect(jsonPath("$.data.timeCapsules[1].title").value("먼저 즐겨찾기한 타임캡슐"));
    }

    @Test
    void 타임캡슐_즐겨찾기에_성공한다() throws Exception {
        // given
        Long capsuleId = 1L;
        TimeCapsuleFavoriteRequest request = new TimeCapsuleFavoriteRequest(
                true
        );

        TimeCapsuleFavoriteResponse response = new TimeCapsuleFavoriteResponse(
                true, LocalDateTime.now(), 10
        );

        given(timeCapsuleService.setFavorite(eq(capsuleId), any(TimeCapsuleFavoriteRequest.class), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/time-capsule/{capsuleId}/favorite", capsuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.ADD_FAVORITE_TIME_CAPSULE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.isFavorite").value(true))
                .andExpect(jsonPath("$.data.favoriteAt", notNullValue()))
                .andExpect(jsonPath("$.data.favoritesCnt").value(10));
    }

    @Test
    void 타임캡슐_즐겨찾기_해제에_성공한다() throws Exception {
        // given
        Long capsuleId = 1L;
        TimeCapsuleFavoriteRequest request = new TimeCapsuleFavoriteRequest(
                false
        );

        TimeCapsuleFavoriteResponse response = new TimeCapsuleFavoriteResponse(
                false, null, 9
        );

        given(timeCapsuleService.setFavorite(eq(capsuleId), any(TimeCapsuleFavoriteRequest.class), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/time-capsule/{capsuleId}/favorite", capsuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.REMOVE_FAVORITE_TIME_CAPSULE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.isFavorite").value(false))
                .andExpect(jsonPath("$.data.favoriteAt", nullValue()))
                .andExpect(jsonPath("$.data.favoritesCnt").value(9));
    }

    @Test
    void 타임캡술_즐겨찾기_상한_초과일_때_예외를_반환한다() throws Exception {
        // given
        Long capsuleId = 1L;
        TimeCapsuleFavoriteRequest request = new TimeCapsuleFavoriteRequest(
                true
        );

        given(timeCapsuleService.setFavorite(eq(capsuleId), any(TimeCapsuleFavoriteRequest.class), anyLong()))
                .willThrow(new BaseException(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED));

        // when & then
        mockMvc.perform(patch("/api/v1/time-capsule/{capsuleId}/favorite", capsuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED.getMessage()));
    }

    @Test
    void 타임캡슐_상세_정보를_반환한다() throws Exception {
        // given
        Long capsuleId = 1L;

        TimeCapsuleDetailResponse response = new TimeCapsuleDetailResponse(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                "도착",
                LocalDateTime.now().minusMinutes(10),
                true,
                "오늘 아침에 ...",
                "오늘 아침에는 ...",
                List.of(
                        new EmotionDetailDto(
                        "기쁨", 40
                        ),
                        new EmotionDetailDto(
                                "뿌듯함", 50
                        )
                ),
                List.of(
                        "코멘트1", "코멘트2"
                ),
                ""
        );

        given(timeCapsuleService.getTimeCapsuleDetail(eq(capsuleId), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/time-capsule/{capsuleId}", capsuleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_TIME_CAPSULE_DETAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.historyDate", notNullValue()))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
                .andExpect(jsonPath("$.data.status").value("도착"))
                .andExpect(jsonPath("$.data.openAt", notNullValue()))
                .andExpect(jsonPath("$.data.isFavorite").value(true))
                .andExpect(jsonPath("$.data.title").value("오늘 아침에 ..."))
                .andExpect(jsonPath("$.data.summary").value("오늘 아침에는 ..."))
                .andExpect(jsonPath("$.data.emotionDetails[0].label").value("기쁨"))
                .andExpect(jsonPath("$.data.emotionDetails[0].ratio").value(40))
                .andExpect(jsonPath("$.data.emotionDetails[1].label").value("뿌듯함"))
                .andExpect(jsonPath("$.data.emotionDetails[1].ratio").value(50))
                .andExpect(jsonPath("$.data.comments[0]").value("코멘트1"))
                .andExpect(jsonPath("$.data.comments[1]").value("코멘트2"))
                .andExpect(jsonPath("$.data.note").value(""));
    }

    @Test
    void 타임캡슐_열람에_성공한다() throws Exception {
        // given
        Long capsuleId = 1L;
        willDoNothing().given(timeCapsuleService).openTimeCapsule(eq(capsuleId), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/time-capsule/{capsuleId}/open", capsuleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.OPEN_TIME_CAPSULE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 타임캡슐_마음노트_수정에_성공한다() throws Exception {
        // given
        Long capsuleId = 1L;
        TimeCapsuleNoteUpdateRequest request = new TimeCapsuleNoteUpdateRequest(
                "타임캡슐 마음노트 업데이트"
        );

        willDoNothing().given(timeCapsuleService).updateMindNote(eq(capsuleId), eq(request), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/time-capsule/{capsuleId}/note", capsuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_TIME_CAPSULE_MIND_NOTE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 타임캡슐_삭제에_성공한다() throws Exception {
        // given
        Long capsuleId = 1L;
        willDoNothing().given(timeCapsuleService).deleteTimeCapsule(eq(capsuleId), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/time-capsule/{capsuleId}", capsuleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.DELETE_TIME_CAPSULE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}