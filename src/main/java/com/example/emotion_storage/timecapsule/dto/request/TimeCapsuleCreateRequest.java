package com.example.emotion_storage.timecapsule.dto.request;

public record TimeCapsuleCreationRequest(
        Long chatRoomId,
        String sessionId
) {}
