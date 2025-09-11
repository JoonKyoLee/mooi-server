package com.example.emotion_storage.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewNotificationResponse {
    
    private final boolean hasNewNotification;
    private final int count;
}
