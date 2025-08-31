package com.example.emotion_storage.user.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.request.KakaoLoginRequest;
import com.example.emotion_storage.user.dto.request.KakaoSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import com.example.emotion_storage.user.dto.response.SignupResponse;
import com.example.emotion_storage.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "구글 로그인", description = "구글 ID 토큰으로 로그인합니다. 리프레시 토큰은 HttpOnly 쿠키로 설정됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "회원가입 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 ID 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "다른 소셜로 이미 가입")
    })
    @PostMapping("/login/google")
    public ApiResponse<LoginResponse> loginWithGoogle(
            @RequestBody GoogleLoginRequest request, HttpServletResponse httpServletResponse) {
        LoginResponse response = userService.googleLogin(request, httpServletResponse);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.LOGIN_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "구글 회원가입", description = "구글 ID 토큰 검증 후 신규 회원을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 ID 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    @PostMapping("/signup/google")
    public ApiResponse<SignupResponse> signupWithGoogle(@RequestBody GoogleSignUpRequest request) {
        userService.googleSignUp(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.SIGNUP_SUCCESS.getMessage(), SignupResponse.ok());
    }

    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인합니다. 리프레시 토큰은 HttpOnly 쿠키로 설정됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "회원가입 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            //@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "다른 소셜로 이미 가입")
    })
    @PostMapping("/login/kakao")
    public ApiResponse<LoginResponse> loginWithKakao(
            @RequestBody KakaoLoginRequest request, HttpServletResponse httpServletResponse) {
        LoginResponse response = userService.kakaoLogin(request, httpServletResponse);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.LOGIN_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "카카오 회원가입", description = "카카오 액세스 토큰 검증 후 신규 회원을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 계정")
    })
    @PostMapping("/signup/kakao")
    public ApiResponse<SignupResponse> signupWithKakao(@RequestBody KakaoSignUpRequest request) {
        userService.kakaoSignUp(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.SIGNUP_SUCCESS.getMessage(), SignupResponse.ok());
    }
}
