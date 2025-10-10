package com.example.emotion_storage.timecapsule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiTimeCapsuleCreateRequest(
        @JsonProperty("role_message")
        String roleMessage,

        @JsonProperty("reference_message")
        String referenceMessage,

        @JsonProperty("analyze_message")
        String analyzeMessage,

        @JsonProperty("session_id")
        String sessionId
) {}
