package com.example.emotion_storage.timecapsule.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public record TimeCapsuleCreationResponse(
        String title,
        String oneLineSummary,
        String dialogueSummary,
        List<String> emotionKeywords,
        List<String> aiFeedback

) {
    public static TimeCapsuleCreationResponse from(
            LocalDateTime historyDate, AiTimeCapsuleCreationResponse response) {
        return new TimeCapsuleCreationResponse(
                formatDateTime(historyDate),
                response.summaryLine(),
                response.summaryBlock(),
                response.keywords(),
                splitFeedback(response.emotionFeedback())
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * 긴 AI 피드백 문자열을 문장 단위(List<String>)로 분리
     *
     * 규칙:
     * - 문장부호(., !, ?, 。, ！, ？) 뒤에서 분리
     * - 줄바꿈(\n)도 분리 기준
     * - 빈 문자열은 제외
     */
    private static List<String> splitFeedback(String feedback) {
        if (feedback == null || feedback.isBlank()) return List.of();

        return Arrays.stream(feedback.split("(?<=[.!?！？])\\s+|\\n+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
