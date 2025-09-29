package com.example.emotion_storage.mypage.dto.response;

public record MyPageOverviewResponse(
        String nickname,
        long days,
        long keys
) {}
