package com.example.emotion_storage.notification.dto;

import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.notification.domain.NotificationType;

public record NotificationDataDto(
        NotificationType type,
        Long targetId
) {
    public static NotificationDataDto from(Notification notification) {
        return new NotificationDataDto(
                notification.getType(),
                notification.getTargetId()
        );
    }
}
