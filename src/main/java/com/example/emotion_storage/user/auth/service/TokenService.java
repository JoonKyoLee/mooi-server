package com.example.emotion_storage.user.auth.service;

import com.example.emotion_storage.global.security.jwt.JwtTokenProvider;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public String issueAccessToken(Long userId) {
        return jwtTokenProvider.generateAccessToken(userId);
    }

    public void issueRefreshToken(Long userId, HttpServletResponse response) {
        String refreshToken = UUID.randomUUID().toString();
        redisService.saveRefreshToken(userId.toString(), refreshToken);

        setRefreshTokenCookie(response, refreshToken);
    }

    public String reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        String userId = redisService.getUserIdByRefreshToken(refreshToken);

        if (userId == null) {
            throw new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        String newRefreshToken = UUID.randomUUID().toString();
        redisService.rotateRefreshToken(userId, refreshToken, newRefreshToken);

        setRefreshTokenCookie(response, newRefreshToken);

        return issueAccessToken(Long.parseLong(userId));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_PREFIX, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_PREFIX.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }
}
