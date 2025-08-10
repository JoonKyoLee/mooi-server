package com.example.emotion_storage.user.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    private static final String USER_ID = "1";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private RedisService redisService;


    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(redisService, "refreshTokenExpiration", 14);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private static String userKey(String userId) { return "refresh:user" + userId; }
    private static String tokenKey(String token) { return "refresh:token" + token; }

    @Test
    void 리프레시_토큰_저장_시에_유저키와_토큰키에_모두_저장한다() {
        // when
        redisService.saveRefreshToken(USER_ID, REFRESH_TOKEN);

        // then
        verify(valueOperations).set(eq(userKey(USER_ID)), eq(REFRESH_TOKEN), any(Duration.class));
        verify(valueOperations).set(eq(tokenKey(REFRESH_TOKEN)), eq(USER_ID), any(Duration.class));
    }

    @Test
    void userId로_리프레시_토큰을_조회하면_리프레시_토큰을_반환한다() {
        // given
        when(valueOperations.get(userKey(USER_ID))).thenReturn(REFRESH_TOKEN);

        // when
        String refreshToken = redisService.getRefreshTokenByUserId(USER_ID);

        // then
        assertThat(refreshToken).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰으로_유저를_조회하면_userId를_반환한다() {
        // given
        when(valueOperations.get(tokenKey(REFRESH_TOKEN))).thenReturn(USER_ID);

        // when
        String userId = redisService.getUserIdByRefreshToken(REFRESH_TOKEN);

        // then
        assertThat(userId).isEqualTo(USER_ID);
    }

    @Test
    void userId로_삭제를_하면_연결된_토큰키도_삭제한다() {
        // given
        when(valueOperations.get(userKey(USER_ID))).thenReturn(REFRESH_TOKEN);

        // when
        redisService.deleteByUserId(USER_ID);

        // then
        verify(redisTemplate).delete(tokenKey(REFRESH_TOKEN));
        verify(redisTemplate).delete(userKey(USER_ID));
    }

    @Test
    void 리프레시_토큰으로_삭제를_하면_연결된_유저키도_삭제한다() {
        // given
        when(valueOperations.get(tokenKey(REFRESH_TOKEN))).thenReturn(USER_ID);

        // when
        redisService.deleteByRefreshToken(REFRESH_TOKEN);

        // then
        verify(redisTemplate).delete(userKey(USER_ID));
        verify(redisTemplate).delete(tokenKey(REFRESH_TOKEN));
    }

    @Test
    void 토큰키와_유저키를_동시에_삭제한다() {
        // when
        redisService.deleteRefreshToken(USER_ID, REFRESH_TOKEN);

        // then
        verify(redisTemplate).delete(userKey(USER_ID));
        verify(redisTemplate).delete(tokenKey(REFRESH_TOKEN));
    }

    @Test
    void 이전_리프레시_토큰을_삭제하고_새로운_리프레시_토큰을_저장한다() {
        // when
        redisService.rotateRefreshToken(USER_ID, REFRESH_TOKEN, NEW_REFRESH_TOKEN);

        // then
        verify(redisTemplate).delete(tokenKey(REFRESH_TOKEN));
        verify(redisTemplate).delete(userKey(USER_ID));
        verify(valueOperations).set(eq(userKey(USER_ID)), eq(NEW_REFRESH_TOKEN), any(Duration.class));
        verify(valueOperations).set(eq(tokenKey(NEW_REFRESH_TOKEN)), eq(USER_ID), any(Duration.class));
    }
}
