package com.example.emotion_storage.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GaugeDto {
    @JsonProperty("gauge_score")
    private int gaugeScore;
    
    @JsonProperty("turn_count_score")
    private int turnCountScore;
    
    @JsonProperty("emotion_expression_score")
    private int emotionExpressionScore;
    
    @JsonProperty("emotion_diversity_score")
    private int emotionDiversityScore;
    
    @JsonProperty("event_reference_score")
    private int eventReferenceScore;
    
    @JsonProperty("emotion_change_score")
    private int emotionChangeScore;
    
    private String summary;

    @Override
    public String toString() {
        return String.format("GaugeDto{gaugeScore=%d, turnCountScore=%d, emotionExpressionScore=%d, emotionDiversityScore=%d, eventReferenceScore=%d, emotionChangeScore=%d, summary='%s'}", 
                gaugeScore, turnCountScore, emotionExpressionScore, emotionDiversityScore, eventReferenceScore, emotionChangeScore, summary);
    }
}
