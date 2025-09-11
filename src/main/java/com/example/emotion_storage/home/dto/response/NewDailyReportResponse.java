package com.example.emotion_storage.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewDailyReportResponse {
    
    private final boolean hasNewReport;
    private final int count;
}
