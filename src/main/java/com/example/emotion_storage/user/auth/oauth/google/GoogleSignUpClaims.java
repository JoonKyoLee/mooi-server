package com.example.emotion_storage.user.auth.oauth.google;

public record GoogleSignUpClaims(
        String subject,
        String email,
        String profileImgUrl
) {}
