package com.example.emotion_storage.timecapsule.dto.response;

import com.example.emotion_storage.timecapsule.domain.AnalyzedFeedback;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.domain.TimeCapsuleStatus;
import com.example.emotion_storage.timecapsule.dto.EmotionDetailDto;
import java.time.LocalDateTime;
import java.util.List;

public record TimeCapsuleDetailResponse(
        Long id,
        LocalDateTime historyDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String status,
        LocalDateTime openAt,
        Boolean isFavorite,
        String title,
        String summary,
        List<EmotionDetailDto> emotionDetails,
        List<String> comments,
        String note
) {
    public static TimeCapsuleDetailResponse from(TimeCapsule entity) {
        TimeCapsuleStatus timeCapsuleStatus = TimeCapsuleStatus.getStatus(entity);

        return new TimeCapsuleDetailResponse(
                entity.getId(),
                entity.getHistoryDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                timeCapsuleStatus.getStatusMessage(),
                entity.getOpenedAt(),
                entity.getIsFavorite(),
                entity.getOneLineSummary(),
                entity.getDialogueSummary(),
                entity.getAnalyzedEmotions()
                        .stream()
                        .map(emotions -> new EmotionDetailDto(emotions.getAnalyzedEmotion(), emotions.getPercentage()))
                        .toList(),
                entity.getAnalyzedFeedbacks()
                        .stream()
                        .map(AnalyzedFeedback::getAnalyzedFeedback)
                        .toList(),
                entity.getMyMindNote()
        );
    }
}
