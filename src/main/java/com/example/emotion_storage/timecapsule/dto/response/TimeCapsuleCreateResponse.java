package com.example.emotion_storage.timecapsule.dto.response;

import com.example.emotion_storage.timecapsule.dto.EmotionDetailDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TimeCapsuleCreateResponse(
        String title,
        String oneLineSummary,
        String dialogueSummary,
        List<EmotionDetailDto> emotionKeywords,
        List<String> aiFeedback,
        LocalDateTime historyDate // 오픈일 계산에 필요
) {
    public static TimeCapsuleCreateResponse from(
            LocalDateTime historyDate, AiTimeCapsuleCreateResponse response) {
        return new TimeCapsuleCreateResponse(
                formatDateTime(historyDate),
                response.summaryLine(),
                response.summaryBlock(),
                parseEmotions(response.keywords()),
                splitFeedback(response.emotionFeedback()),
                historyDate
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        return dateTime.format(formatter);
    }

    private static List<EmotionDetailDto> parseEmotions(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return List.of();

        Pattern tailPercent = Pattern.compile("(\\d{1,3})\\s*%\\s*$");

        return keywords.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(line -> {
                    Matcher matcher = tailPercent.matcher(line);
                    int ratio = 0;
                    String label = line;

                    if (matcher.find()) {
                        try {
                            ratio = Integer.parseInt(matcher.group(1));
                        } catch (NumberFormatException e) {}
                        label = line.substring(0, matcher.start()).trim();
                    }
                    return new EmotionDetailDto(label, ratio);
                })
                .filter(e -> !e.label().isEmpty())
                .toList();
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
