package com.example.emotion_storage.mypage.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.service.MyPageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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
}
