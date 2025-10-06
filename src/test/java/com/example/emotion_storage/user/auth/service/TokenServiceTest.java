package com.example.emotion_storage.user.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RedisService redisService;
    @Mock private HttpServletResponse response;
    @Mock private HttpServletRequest request;
    @InjectMocks private TokenService tokenService;

    @Captor private ArgumentCaptor<String> headerNameCaptor;
    @Captor private ArgumentCaptor<String> headerValueCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", 14);
    }

    @Test
    void user_id로_액세스_토큰을_발급해_반환한다() {
        // given
        Long userId = 1L;

        when(jwtTokenProvider.generateAccessToken(1L)).thenReturn("access-token");

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(userId);

        // then
        assertThat(accessToken).isEqualTo("access-token");
    }

    @Test
    void 리프레시_토큰을_발급하고_쿠키에_저장한다() {
        // given
        Long userId = 5L;

        // when
        tokenService.issueRefreshToken(userId, response);

        // then
        verify(redisService).saveRefreshToken(eq(userId.toString()), anyString());
        verify(response).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertThat(headerNameCaptor.getValue()).isEqualTo("Set-Cookie");
        assertThat(headerValueCaptor.getValue())
                .contains("refreshToken=")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None")
                .contains("Path=/");
    }

    @Test
    void 리프레시_토큰이_유효하면_액세스_토큰을_재발급한다() {
        // given
        Cookie[] cookies = {new Cookie("refreshToken", "old-refresh")};
        when(request.getCookies()).thenReturn(cookies);
        when(redisService.getUserIdByRefreshToken(eq("old-refresh"))).thenReturn("2");
        when(jwtTokenProvider.generateAccessToken(eq(2L))).thenReturn("new-access-token");

        // when
        String newAccessToken = tokenService.reissueAccessToken(request, response);

        // then
        assertThat(newAccessToken).isEqualTo("new-access-token");
        verify(redisService).rotateRefreshToken(eq("2"), eq("old-refresh"), anyString());
        verify(response).addHeader(eq("Set-Cookie"), contains("refreshToken="));
    }

    @Test
    void 쿠키가_null일_때_예외가_발생한다() {
        // given
        when(request.getCookies()).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    void 쿠키에_refresh_token_정보가_없을_때_예외가_발생한다() {
        // given
        Cookie[] cookies = {new Cookie("no-name", "no-value")};
        when(request.getCookies()).thenReturn(cookies);

        // when & then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    void redis에_userId가_없을_때_예외가_발생한다() {
        // given
        Cookie[] cookies = {new Cookie("refreshToken", "refresh-token")};
        when(request.getCookies()).thenReturn(cookies);
        when(redisService.getUserIdByRefreshToken(eq("refresh-token"))).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    void 리프레시_토큰과_액세스_토큰을_무효화한다() {
         // given
        Long userId = 10L;
        String accessToken = "access-token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + accessToken);
        when(jwtTokenProvider.getRemainingMillis(accessToken)).thenReturn(3600L);

        // when
        tokenService.revokeTokens(request, response, userId);

        // then
        verify(redisService).deleteByUserId(eq(userId.toString()));
        verify(redisService).addAccessTokenToBlacklist(eq(accessToken), eq(3600L));

        verify(response).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());
        assertThat(headerNameCaptor.getValue()).isEqualTo("Set-Cookie");
        assertThat(headerValueCaptor.getValue())
                .contains("refreshToken=")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None")
                .contains("Path=/")
                .contains("Max-Age=0");
    }
}
