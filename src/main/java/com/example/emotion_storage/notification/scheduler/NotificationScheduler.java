package com.example.emotion_storage.notification.scheduler;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.notification.service.NotificationService;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final int BATCH_SIZE = 200;

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final NotificationService notificationService;

    /**
     * 타임캡슐 도착 알림 생성
     * 06:00 ~ 23:59 (KST)
     * 매 1분마다 실행
     */
    @Scheduled(cron = "0 * 6-23 * * ?", zone = "Asia/Seoul")
    @Transactional
    public void createTimeCapsuleArrivalNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int page = 0;

        while (true) {
            Pageable pageable = PageRequest.of(page, BATCH_SIZE);
            Page<TimeCapsule> capsules = timeCapsuleRepository.findArrivalTargetCapsules(now, pageable);

            if (capsules.isEmpty()) {
                break;
            }

            log.debug("도착 대상 타임캡슐 조회 - page={}, size={}", page, capsules.getNumberOfElements());

            for (TimeCapsule capsule : capsules.getContent()) {
                Long capsuleId = capsule.getId();
                Long userId = null;

                try {
                    userId = capsule.getUser().getId();
                    notificationService.createTimeCapsuleArrival(userId, capsuleId);
                } catch (BaseException exception) {
                    if (exception.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                        log.warn("탈퇴/삭제 유저로 인해 알림 생성 패스 - capsuleId: {}, userId: {}", capsuleId, userId);
                        continue;
                    }
                    log.error("알림 생성 BaseException - capsuleId: {}, userId: {}, code: {}",
                            capsuleId, userId, exception.getErrorCode(), exception);
                } catch (Exception exception) {
                    log.error("알림 생성 중 예기치 못한 오류 - capsuleId={}, userId={}", capsuleId, userId, exception);
                }
            }

            if (!capsules.hasNext()) {
                break;
            }
            page++;
        }
        log.info("타임캡슐 도착 알림 스케줄러 종료");
    }
}
