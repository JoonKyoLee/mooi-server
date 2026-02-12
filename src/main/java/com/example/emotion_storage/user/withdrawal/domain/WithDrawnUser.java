package com.example.emotion_storage.user.withdrawal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "withdrawn_users",
        indexes = {
                @Index(name = "idx_withdrawn_users_status_purge_after", columnList = "purge_status,purge_after"),
                @Index(name = "idx_withdrawn_users_purge_after", columnList = "purge_after")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WithDrawnUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawn_user_id")
    private Long id;

    /**
     * 탈퇴/삭제 배치 테이블은 관계 탐색이 목적이 아니라
     * 대량 purge 성능(인덱스/쿼리/락 최소화)이 목적이라 user 엔티티 연관관계를 두지 않음
     * - 배치에서 User 로딩 불필요
     * - Lazy 로딩/프록시/조인 비용 제거
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    @Column(name = "purge_after", nullable = false)
    private LocalDateTime purgeAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "purge_status", nullable = false)
    private PurgeStatus purgeStatus;

    @Column(name = "purged_at")
    private LocalDateTime purgedAt;

    @Column(name = "fail_reason", length = 255)
    private String failReason;

    public static WithDrawnUser pending(Long userId, LocalDateTime now, LocalDateTime purgeAfter) {
        return WithDrawnUser.builder()
                .userId(userId)
                .withdrawnAt(now)
                .purgeAfter(purgeAfter)
                .purgeStatus(PurgeStatus.PENDING)
                .build();
    }

    public void markSuccess(LocalDateTime purgedAt) {
        this.purgeStatus = PurgeStatus.SUCCESS;
        this.purgedAt = purgedAt;
        this.failReason = null;
    }

    public void markFailed(String reason) {
        this.purgeStatus = PurgeStatus.FAILED;
        this.purgedAt = null;
        this.failReason = reason;
    }
}
