package com.example.emotion_storage.timecapsule.dto.request;

public record TimeCapsuleCreateRequest(
        Long chatroomId,
        String sessionId
) {}
