package com.example.emotion_storage.timecapsule.dto.request;

import com.example.emotion_storage.timecapsule.dto.EmotionDetailDto;
import java.time.LocalDateTime;
import java.util.List;

public record TimeCapsuleSaveRequest(
        Long chatroomId,
        String oneLineSummary,
        String dialogueSummary,
        LocalDateTime openAt, // 임시 저장일 경우에는 null
        List<EmotionDetailDto> emotionKeywords,
        List<String> aiFeedback,
        boolean isTempSave
) {}
