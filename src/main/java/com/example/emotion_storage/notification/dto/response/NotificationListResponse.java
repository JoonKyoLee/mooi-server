package com.example.emotion_storage.notification.dto.response;

import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.notification.dto.NotificationDto;
import com.example.emotion_storage.global.dto.PaginationDto;
import java.util.List;
import org.springframework.data.domain.Page;

public record NotificationListResponse(
        PaginationDto pagination,
        int totalNotifications,
        List<NotificationDto> notifications
) {
    public static NotificationListResponse of(Page<Notification> notifications, int page, int limit) {
        return new NotificationListResponse(
                new PaginationDto(
                        page, limit, notifications.getTotalPages()
                ),
                notifications.getNumberOfElements(),
                notifications.getContent().stream()
                        .map(NotificationDto::from)
                        .toList()
        );
    }
}
