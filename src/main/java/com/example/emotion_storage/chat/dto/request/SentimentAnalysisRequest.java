package com.example.emotion_storage.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentAnalysisRequest {
    @JsonProperty("role_message")
    private String roleMessage;
    
    @JsonProperty("reference_message")
    private String referenceMessage;
    
    @JsonProperty("analyze_message")
    private String analyzeMessage;
}
