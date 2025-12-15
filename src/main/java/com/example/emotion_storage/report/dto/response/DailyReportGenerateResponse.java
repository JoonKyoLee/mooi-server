package com.example.emotion_storage.report.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportGenerateResponse {

    private List<String> summaries;

    private List<String> keywords;

    @JsonProperty("sentiment_changes")
    private List<String> sentimentChanges;

    @JsonProperty("stress_level")
    private Integer stressLevel;

    @JsonProperty("happiness_level")
    private Integer happinessLevel;

    @JsonProperty("sentiment_review")
    private String sentimentReview;
}

