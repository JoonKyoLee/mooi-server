package com.example.emotion_storage.notification.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.notification.domain.NotificationType;
import com.example.emotion_storage.notification.dto.response.NotificationListResponse;
import com.example.emotion_storage.notification.repository.NotificationRepository;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    @Transactional
    public void createTimeCapsuleArrival(Long userId, Long timeCapsuleId) {
        createTargetNotificationIfAbsent(userId, NotificationType.TIME_CAPSULE_ARRIVAL, timeCapsuleId);
    }

    @Transactional
    public void createDailyReportArrival(Long userId, Long reportId) {
        createTargetNotificationIfAbsent(userId, NotificationType.DAILY_REPORT_ARRIVAL, reportId);
    }

    private void createTargetNotificationIfAbsent(Long userId, NotificationType type, Long targetId) {
        if (notificationRepository.existsByUser_IdAndTypeAndTargetId(userId, type, targetId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .title(type.title())
                .content(type.content())
                .isRead(false)
                .type(type)
                .targetId(targetId)
                .arrivedAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
