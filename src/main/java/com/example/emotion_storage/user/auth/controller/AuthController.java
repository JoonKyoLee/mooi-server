package com.example.emotion_storage.user.auth.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.user.auth.dto.response.AccessTokenResponse;
import com.example.emotion_storage.user.auth.dto.response.SessionResponse;
import com.example.emotion_storage.user.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "액세스 토큰 재발급 API")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;

    @Operation(summary = "액세스 토큰 재발급", description = "HttpOnly 쿠키의 리프레시 토큰으로 액세스 토큰을 재발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "리프레시 토큰 없음/만료")
    })
    @PostMapping("/reissue")
    public ApiResponse<AccessTokenResponse> reissueAccessToken(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String accessToken = tokenService.reissueAccessToken(httpServletRequest, httpServletResponse);
        AccessTokenResponse response = AccessTokenResponse.from(accessToken);
        return ApiResponse.success(HttpStatus.OK.value(), SuccessMessage.ACCESS_TOKEN_REISSUE_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "세션 유지 확인", description = "Authorization 헤더의 액세스 토큰이 유효한지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "세션 정보 확인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "액세스 토큰 만료")
    })
    @GetMapping("/session")
    public ApiResponse<SessionResponse> checkSession() {
        return ApiResponse.success(SuccessMessage.SESSION_CHECK_SUCCESS.getMessage(), SessionResponse.ok());
    }
}
