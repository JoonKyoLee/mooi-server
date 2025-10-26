package com.example.emotion_storage.timecapsule.dto.request;

import java.time.LocalDateTime;

public record TimeCapsuleOpenDateUpdateRequest(
        Long capsuleId,
        LocalDateTime storedAt,
        LocalDateTime openAt
) {}
