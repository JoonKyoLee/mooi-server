package com.example.emotion_storage.mypage.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.request.NotificationSettingsUpdateRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.mypage.dto.response.UserKeyCountResponse;
import com.example.emotion_storage.mypage.service.MyPageService;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MyPageController.class)
@Import(TestSecurityConfig.class)
public class MyPageControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean MyPageService myPageService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 마이페이지_초기_화면_관련_정보를_반환한다() throws Exception {
        // given
        MyPageOverviewResponse response = new MyPageOverviewResponse(
                "MOOI", 200L, 10L
        );

        given(myPageService.getMyPageOverview(anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/mypage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_MY_PAGE_USER_INFO_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.nickname").value("MOOI"))
                .andExpect(jsonPath("$.data.days").value(200L))
                .andExpect(jsonPath("$.data.keys").value(10L));
    }

    @Test
    void 닉네임_수정에_성공한다() throws Exception {
        // given
        NicknameChangeRequest request = new NicknameChangeRequest("모이");
        willDoNothing().given(myPageService).changeUserNickname(eq(request), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/mypage/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.CHANGE_USER_NICKNAME_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 닉네임이_조건에_맞지_않으면_예외가_발생한다() throws Exception {
        // given
        NicknameChangeRequest request = new NicknameChangeRequest("1");
        willThrow(new BaseException(ErrorCode.INVALID_NICKNAME))
                .given(myPageService).changeUserNickname(eq(request), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/mypage/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 열쇠_개수_조회에_성공한다() throws Exception {
        // given
        UserKeyCountResponse response = new UserKeyCountResponse(10L);

        given(myPageService.getUserKeyCount(anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_USER_KEY_COUNT_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.keyCount").value(10L));
    }

    @Test
    void 사용자_계정_정보_조회에_성공한다() throws Exception {
        // given
        UserAccountInfoResponse response = new UserAccountInfoResponse(
                "MOOI", "test@example.com", SocialType.GOOGLE, Gender.MALE, LocalDate.of(2000, 1, 1), 200
        );

        given(myPageService.getUserAccountInfo(anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_USER_ACCOUNT_INFO_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.nickname").value("MOOI"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.socialType").value("GOOGLE"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthday").value("2000-01-01"))
                .andExpect(jsonPath("$.data.days").value(200));
    }

    @Test
    void 사용자_알림설정_상태_정보_조회에_성공한다() throws Exception {
        // given
        NotificationSettingsResponse response = new NotificationSettingsResponse(
                true, true, Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                LocalTime.of(21, 0), true, true
        );

        given(myPageService.getNotificationSettings(anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/notification-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.GET_NOTIFICATION_SETTINGS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.appPushNotify").value(true))
                .andExpect(jsonPath("$.data.emotionReminderNotify").value(true))
                .andExpect(jsonPath("$.data.emotionReminderDays", containsInAnyOrder("MONDAY", "TUESDAY")))
                .andExpect(jsonPath("$.data.emotionReminderTime").value("21:00:00"))
                .andExpect(jsonPath("$.data.timeCapsuleReportNotify").value(true))
                .andExpect(jsonPath("$.data.marketingInfoNotify").value(true));
    }

    @Test
    void 사용자_알림_설정_상태_업데이트에_성공한다() throws Exception {
        // given
        NotificationSettingsUpdateRequest request = new NotificationSettingsUpdateRequest(
                true, true, Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                LocalTime.of(21, 0), true, true
        );
        willDoNothing().given(myPageService).updateNotificationSettings(eq(request), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/mypage/notification-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_NOTIFICATION_SETTINGS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 회원_탈퇴에_성공한다() throws Exception {
        // given
        willDoNothing().given(myPageService).withdrawUser(anyLong(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/mypage/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WITHDRAW_USER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 로그아웃에_성공한다() throws Exception {
        // given
        willDoNothing().given(myPageService).logout(anyLong(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/mypage/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.NO_CONTENT.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.LOGOUT_USER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
