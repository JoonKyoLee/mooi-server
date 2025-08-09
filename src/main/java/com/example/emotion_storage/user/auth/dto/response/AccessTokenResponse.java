package com.example.emotion_storage.user.auth.dto.response;

public record AccessTokenResponse(
        String accessToken
) {
    public static AccessTokenResponse from(String accessToken) {
        return new AccessTokenResponse(accessToken);
    }
}
