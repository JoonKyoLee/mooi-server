package com.example.emotion_storage.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeInfoResponse {
    
    private final Long remainingTickets;
    private final Long dailyLimit;
    private final Long keyCount;
    private final boolean hasNewNotification;
    private final int notificationCount;
    private final boolean hasNewTimeCapsule;
    private final int timeCapsuleCount;
    private final boolean hasNewReport;
    private final int reportCount;
}
