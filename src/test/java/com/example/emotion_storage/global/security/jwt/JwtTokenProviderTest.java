package com.example.emotion_storage.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtTokenProviderTest {

    private static final String SECRET = "0123456789ABCDEF0123456789ABCDEF";
    private static final int EXP_MINUTES = 10;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXP_MINUTES);
    }

    @Test
    void 유효한_토큰을_생성한다() {
        // given
        Long userId = 1L;

        // when
        String token = jwtTokenProvider.generateAccessToken(userId);

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    void 유효한_토큰_생성_시에_Claims에_userId가_들어간다() {
        // given
        Long userId = 1L;

        // when
        String token = jwtTokenProvider.generateAccessToken(userId);
        Long parsedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(parsedUserId).isEqualTo(userId);
    }

    @Test
    void 토큰이_유효할_때_VALID를_반환한다() {
        // given
        Long userId = 1L;
        String token = jwtTokenProvider.generateAccessToken(userId);

        // when
        TokenStatus status = jwtTokenProvider.validateToken(token);

        // then
        assertThat(status).isEqualTo(TokenStatus.VALID);
    }

    @Test
    void 토큰이_만료_되었을_때_EXPIRED를_반환한다() {
        // given
        JwtTokenProvider immediateExpireJwtTokenProvider = new JwtTokenProvider(SECRET, 0);
        Long userId = 1L;

        // when
        String token = immediateExpireJwtTokenProvider.generateAccessToken(userId);
        TokenStatus status = immediateExpireJwtTokenProvider.validateToken(token);

        // then
        assertThat(status).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    void 유효하지_않은_토큰일_때_INVALID를_반환한다() {
        assertThat(jwtTokenProvider.validateToken("strange-token")).isEqualTo(TokenStatus.INVALID);
    }
}
