package com.example.emotion_storage.timecapsule.dto;

import com.example.emotion_storage.timecapsule.domain.AnalyzedEmotion;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import java.time.LocalDateTime;
import java.util.List;

public record TimeCapsuleDto(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime openAt,
        Boolean isFavorite,
        List<String> emotions,
        String title
) {
    public static TimeCapsuleDto of(TimeCapsule entity) {
        return new TimeCapsuleDto(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getOpenedAt(),
                entity.getIsFavorite(),
                entity.getAnalyzedEmotions()
                        .stream()
                        .map(AnalyzedEmotion::getAnalyzedEmotion)
                        .toList(),
                entity.getOneLineSummary()
        );
    }
}
