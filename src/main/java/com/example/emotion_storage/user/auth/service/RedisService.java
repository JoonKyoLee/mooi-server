package com.example.emotion_storage.user.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${auth.refresh.expiration-days}")
    private int refreshTokenExpiration;

    private String userKey(String userId) {
        return "refresh:user:" + userId;
    }

    private String tokenKey(String token) {
        return "refresh:token:" + token;
    }

    public void saveRefreshToken(String userId, String refreshToken) {
        Duration ttl = Duration.ofDays(refreshTokenExpiration);
        redisTemplate.opsForValue().set(userKey(userId), refreshToken, ttl);
        redisTemplate.opsForValue().set(tokenKey(refreshToken), userId, ttl);
    }

    public String getRefreshTokenByUserId(String userId) {
        return redisTemplate.opsForValue().get(userKey(userId));
    }

    public String getUserIdByRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get(tokenKey(refreshToken));
    }

    public void deleteByUserId(String userId) {
        String refreshToken = getRefreshTokenByUserId(userId);
        if (refreshToken != null) {
            redisTemplate.delete(tokenKey(refreshToken));
        }
        redisTemplate.delete(userKey(userId));
    }

    public void deleteByRefreshToken(String refreshToken) {
        String userId = getUserIdByRefreshToken(refreshToken);
        if (userId != null) {
            redisTemplate.delete(userKey(userId));
        }
        redisTemplate.delete(tokenKey(refreshToken));
    }

    public void deleteRefreshToken(String userId, String refreshToken) {
        redisTemplate.delete(userKey(userId));
        redisTemplate.delete(tokenKey(refreshToken));
    }

    public void rotateRefreshToken(String userId, String oldToken, String newToken) {
        if (oldToken != null) {
            redisTemplate.delete(tokenKey(oldToken));
        }
        redisTemplate.delete(userKey(userId));
        saveRefreshToken(userId, newToken);
    }
}
