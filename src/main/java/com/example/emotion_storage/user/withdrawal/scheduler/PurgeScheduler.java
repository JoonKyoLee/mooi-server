package com.example.emotion_storage.user.withdrawal.scheduler;

import com.example.emotion_storage.user.withdrawal.domain.PurgeStatus;
import com.example.emotion_storage.user.withdrawal.repository.WithDrawnUserRepository;
import com.example.emotion_storage.user.withdrawal.service.PurgeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurgeScheduler {

    private static final int BATCH_SIZE = 50;

    private final WithDrawnUserRepository withDrawnUserRepository;
    private final PurgeService purgeService;

    /**
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeWithDrawnUser() {
        LocalDateTime now = LocalDateTime.now();

        List<Long> targetIds = withDrawnUserRepository.findPurgeTargetIds(
                PurgeStatus.PENDING, now, PageRequest.of(0, BATCH_SIZE)
        );

        if (targetIds.isEmpty()) {
            log.info("스케줄러 종료: 삭제할 유저 없음");
            return;
        }

        log.info("삭제할 유저 {}명 삭제 시작", targetIds.size());

        for (Long withdrawnId : targetIds) {
            try {
                Long userId = withDrawnUserRepository.findById(withdrawnId)
                        .orElseThrow()
                        .getUserId();

                purgeService.purgeUser(userId);

                withDrawnUserRepository.markSuccessBulk(
                        List.of(withdrawnId), PurgeStatus.SUCCESS, now
                );
            } catch (Exception e) {
                log.error("withdrawnId {} 삭제 실패", withdrawnId);

                withDrawnUserRepository.markFailed(
                        withdrawnId, PurgeStatus.FAILED, e.getMessage()
                );
            }
        }

        log.info("유저 삭제 배치 스케줄러 종료");
    }
}
