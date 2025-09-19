package com.example.emotion_storage.timecapsule.dto;

import com.example.emotion_storage.timecapsule.domain.AnalyzedEmotion;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.domain.TimeCapsuleStatus;
import java.time.LocalDateTime;
import java.util.List;

public record TimeCapsuleDto(
        Long id,
        LocalDateTime historyDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime openAt,
        Boolean isFavorite,
        List<String> emotions,
        String title,
        String timeCapsuleStatus
) {
    public static TimeCapsuleDto of(TimeCapsule entity) {
        TimeCapsuleStatus status = TimeCapsuleStatus.getStatus(entity);

        return new TimeCapsuleDto(
                entity.getId(),
                entity.getHistoryDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getOpenedAt(),
                entity.getIsFavorite(),
                entity.getAnalyzedEmotions()
                        .stream()
                        .map(AnalyzedEmotion::getAnalyzedEmotion)
                        .toList(),
                entity.getOneLineSummary(),
                status.getStatusMessage()
        );
    }
}
