package com.example.emotion_storage.notification.service;

import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.notification.dto.response.NotificationListResponse;
import com.example.emotion_storage.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int RECENT_NOTIFICATION_DAYS = 21;
    private static final String SORT_BY_ARRIVED_TIME = "arrivedAt";

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse fetchNotifications(int page, int limit, Long userId) {
        LocalDateTime start = LocalDateTime.now().minusDays(RECENT_NOTIFICATION_DAYS);
        Pageable pageable = pageDesc(page, limit);

        log.info("사용자 {}의 {}부터의 알림 목록을 조회합니다.", userId, start);
        Page<Notification> notifications =
                notificationRepository.findByUser_IdAndArrivedAtAfter(userId, start, pageable);

        return NotificationListResponse.of(notifications, page, limit);
    }

    private Pageable pageDesc(int page, int limit) {
        int zeroBasedPage = Math.max(0, page - 1);
        return PageRequest.of(zeroBasedPage, limit, Sort.by(SORT_BY_ARRIVED_TIME).descending());
    }
}
