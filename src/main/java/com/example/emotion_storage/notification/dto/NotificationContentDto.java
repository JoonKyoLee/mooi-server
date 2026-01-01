package com.example.emotion_storage.notification.dto;

import com.example.emotion_storage.notification.domain.Notification;

public record NotificationContentDto(
        String title,
        String body
) {
    public static NotificationContentDto from(Notification notification) {
        return new NotificationContentDto(
                notification.getTitle(),
                notification.getContent()
        );
    }
}
