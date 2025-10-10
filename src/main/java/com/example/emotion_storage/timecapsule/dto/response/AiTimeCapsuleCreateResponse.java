package com.example.emotion_storage.timecapsule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiTimeCapsuleCreationResponse(
        String title,

        @JsonProperty("summary_line")
        String summaryLine,

        @JsonProperty("summary_block")
        String summaryBlock,

        List<String> keywords,

        @JsonProperty("emotion_feedback")
        String emotionFeedback
) {}
