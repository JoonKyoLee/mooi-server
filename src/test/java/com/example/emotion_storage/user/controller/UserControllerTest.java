package com.example.emotion_storage.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import com.example.emotion_storage.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    void 구글_로그인_성공할_때는_액세스_토큰을_반환한다() throws Exception {
        // given
        given(userService.googleLogin(any(GoogleLoginRequest.class), any(HttpServletResponse.class)))
                .willReturn(new LoginResponse("access-token"));

        // when, then
        mockMvc.perform(post("/api/v1/users/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessage.LOGIN_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void 구글_로그인_시에_회원가입이_되어있지_않다면_예외가_발생한다() throws Exception {
        // given
        given(userService.googleLogin(any(GoogleLoginRequest.class), any(HttpServletResponse.class)))
                .willThrow(new BaseException(ErrorCode.NEED_SIGN_UP));

        // when, then
        mockMvc.perform(post("/api/v1/users/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("id-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("NEED_SIGN_UP"))
                .andExpect(jsonPath("$.message").value(ErrorCode.NEED_SIGN_UP.getMessage()));
    }

    @Test
    void 구글_로그인_시에_ID_TOKEN이_유효하지_않다면_예외가_발생한다() throws Exception {
        // given
        given(userService.googleLogin(any(GoogleLoginRequest.class), any(HttpServletResponse.class)))
                .willThrow(new BaseException(ErrorCode.INVALID_ID_TOKEN));

        // when, then
        mockMvc.perform(post("/api/v1/users/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_ID_TOKEN"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_ID_TOKEN.getMessage()));
    }

    @Test
    void 구글_로그인_시에_카카오_계정으로_가입된_이메일로_로그인을_시도하면_예외가_발생한다() throws Exception {
        // given
        given(userService.googleLogin(any(GoogleLoginRequest.class), any(HttpServletResponse.class)))
                .willThrow(new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO));

        // when, then
        mockMvc.perform(post("/api/v1/users/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("id-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED_WITH_KAKAO"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO.getMessage()));
    }

    @Test
    void 구글_회원가입이_성공하면_success_true를_반환한다() throws Exception {
        // given
        GoogleSignUpRequest request = new GoogleSignUpRequest(
                "모이",
                Gender.MALE,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                List.of("내 감정을 정리하고 싶어요", "내 감정 패턴을 알고 싶어요"),
                true,
                true,
                false,
                "id-token"
        );

        // when, then
        mockMvc.perform(post("/api/v1/users/signup/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessage.SIGNUP_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void 구글_회원가입_시에_ID_TOKEN이_유효하지_않다면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BaseException(ErrorCode.INVALID_ID_TOKEN))
                .when(userService)
                .googleSignUp(any(GoogleSignUpRequest.class));

        GoogleSignUpRequest request = new GoogleSignUpRequest(
                "모이",
                Gender.FEMALE,
                LocalDateTime.of(1990, 1, 1, 0, 0),
                List.of("내 감정을 정리하고 싶어요"),
                true,
                true,
                false,
                "id-token"
        );

        // when, then
        mockMvc.perform(post("/api/v1/users/signup/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_ID_TOKEN"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_ID_TOKEN.getMessage()));
    }

    @Test
    void 구글_회원가입_시에_이미_구글로_가입된_이메일로_회원가입을_시도하면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_GOOGLE))
                .when(userService)
                .googleSignUp(any(GoogleSignUpRequest.class));

        GoogleSignUpRequest request = new GoogleSignUpRequest(
                "모이",
                Gender.MALE,
                LocalDateTime.of(2000,1,1,0,0),
                List.of(),
                true, true, false,
                "id-token"
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/signup/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED_WITH_GOOGLE"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_REGISTERED_WITH_GOOGLE.getMessage()));
    }

    @Test
    void 구글_회원가입_시에_이미_카카오로_가입된_이메일로_회원가입을_시도하면_예외가_발생한다() throws Exception {
        // given
        doThrow(new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO))
                .when(userService)
                .googleSignUp(any(GoogleSignUpRequest.class));

        GoogleSignUpRequest request = new GoogleSignUpRequest(
                "모이",
                Gender.FEMALE,
                LocalDateTime.of(2000,1,1,0,0),
                List.of(),
                true, true, false,
                "id-token"
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/signup/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED_WITH_KAKAO"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO.getMessage()));
    }
}
