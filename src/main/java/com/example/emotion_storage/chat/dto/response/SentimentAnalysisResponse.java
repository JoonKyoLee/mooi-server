package com.example.emotion_storage.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentAnalysisResponse {
    private List<String> summaries;
    private List<String> keywords;
    
    @JsonProperty("sentiment_changes")
    private List<String> sentimentChanges;
    
    @JsonProperty("stress_level")
    private int stressLevel;
    
    @JsonProperty("hapiness_level")
    private int happinessLevel;
    
    @JsonProperty("sentiment_review")
    private String sentimentReview;
}
