package com.example.emotion_storage.timecapsule.dto;

import com.example.emotion_storage.global.util.LabelNormalizer;

public record EmotionDetailDto(
        String label,
        int ratio
) {
    public static EmotionDetailDto of(String label, int ratio) {
        return new EmotionDetailDto(
                LabelNormalizer.emojiSpace(label),
                ratio
        );
    }
}
