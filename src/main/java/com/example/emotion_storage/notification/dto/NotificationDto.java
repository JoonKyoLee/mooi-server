package com.example.emotion_storage.notification.dto;

import com.example.emotion_storage.notification.domain.Notification;
import java.time.LocalDateTime;

public record NotificationDto(
        Long notificationId,
        NotificationContentDto notification,
        Boolean isRead,
        LocalDateTime arrivedAt,
        NotificationDataDto data
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                NotificationContentDto.from(notification),
                notification.getIsRead(),
                notification.getArrivedAt(),
                NotificationDataDto.from(notification)
        );
    }
}
