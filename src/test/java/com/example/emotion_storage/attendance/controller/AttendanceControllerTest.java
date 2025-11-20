package com.example.emotion_storage.attendance.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.attendance.response.AttendanceStreakStatusResponse;
import com.example.emotion_storage.attendance.service.AttendanceService;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttendanceController.class)
@Import(TestSecurityConfig.class)
public class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AttendanceService attendanceService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 출석_보상_현황_조회에_성공한다() throws Exception {
        // given
        AttendanceStreakStatusResponse response = new AttendanceStreakStatusResponse(3, true);

        given(attendanceService.getAttendanceRewardStatus(anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_ATTENDANCE_STATUS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.streak").value(3))
                .andExpect(jsonPath("$.data.isAttendedToday").value(true));
    }

    @Test
    void 출석_보상_수령에_성공한다() throws Exception {
        // given
        LocalDate rewardDate = LocalDate.of(2025, 1, 1);
        AttendanceStreakStatusResponse response = new AttendanceStreakStatusResponse(1, true);

        given(attendanceService.updateAttendanceRewardStatus(anyLong(), eq(rewardDate)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/attendance/attend/{rewardDate}", rewardDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_ATTENDANCE_STATUS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.streak").value(1))
                .andExpect(jsonPath("$.data.isAttendedToday").value(true));
    }

    @Test
    void 만료된_날짜로_출석_보상을_요청하면_예외가_발생한다() throws Exception {
        // given
        LocalDate rewardDate = LocalDate.of(2025, 1, 1);

        willThrow(new BaseException(ErrorCode.EXPIRED_ATTENDANCE_REWARD))
                .given(attendanceService).updateAttendanceRewardStatus(anyLong(), eq(rewardDate));

        // when & then
        mockMvc.perform(post("/api/v1/attendance/attend/{rewardDate}", rewardDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_ATTENDANCE_REWARD.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
