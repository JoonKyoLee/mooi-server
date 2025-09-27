package com.example.emotion_storage.mypage.dto.response;

public record UserInfoResponse(
        String nickname,
        long days,
        long keys
) {}
