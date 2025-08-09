package com.example.emotion_storage.user.auth.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.user.auth.dto.response.AccessTokenResponse;
import com.example.emotion_storage.user.auth.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ApiResponse<AccessTokenResponse> reissueAccessToken(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String accessToken = tokenService.reissueAccessToken(httpServletRequest, httpServletResponse);
        AccessTokenResponse response = AccessTokenResponse.from(accessToken);
        return ApiResponse.success(response);
    }
}
