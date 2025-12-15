package com.example.emotion_storage.report.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportGenerateRequest {

    @JsonProperty("role_message")
    private String roleMessage;

    @JsonProperty("reference_message")
    private String referenceMessage;

    @JsonProperty("analyze_message")
    private String analyzeMessage;
}

