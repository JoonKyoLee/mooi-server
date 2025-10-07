package com.example.emotion_storage.mypage.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.UserKeyCountResponse;
import com.example.emotion_storage.mypage.service.MyPageService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
