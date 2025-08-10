package com.example.emotion_storage.user.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.auth.service.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean TokenService tokenService;

    @Test
    void 쿠키에_리프레시_토큰이_존재하면_액세스_토큰을_재발급한다() throws Exception {
        // given
        given(tokenService.reissueAccessToken(any(), any()))
                .willReturn("new-access-token");

        // when, then
        mockMvc.perform(post("/auth/reissue")
                        .cookie(new Cookie("refreshToken", "refresh-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.ACCESS_TOKEN_REISSUE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void 쿠키에_리프레시_토큰이_존재하지_않거나_유효하지_않을_때_예외가_발생한다() throws Exception {
        // given
        given(tokenService.reissueAccessToken(any(), any()))
                .willThrow(new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // when, then
        mockMvc.perform(post("/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }
}
