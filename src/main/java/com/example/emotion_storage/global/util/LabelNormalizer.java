package com.example.emotion_storage.global.util;

public final class LabelNormalizer {
    private LabelNormalizer() {
    }

    public static String emojiSpace(String label) {
        if (label == null || label.isBlank()) {
            return label;
        }
        return label.replaceAll("([\\p{So}])([가-힣])", "$1 $2");
    }
}
