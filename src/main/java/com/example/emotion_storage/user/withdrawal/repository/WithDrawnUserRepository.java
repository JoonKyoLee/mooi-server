package com.example.emotion_storage.user.withdrawal.repository;

import com.example.emotion_storage.user.withdrawal.domain.PurgeStatus;
import com.example.emotion_storage.user.withdrawal.domain.WithDrawnUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WithDrawnUserRepository extends JpaRepository<WithDrawnUser, Long> {

    Optional<WithDrawnUser> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    /**
     * purge 대상 조회 (배치용)
     * - 한 번에 너무 많이 가져오지 않도록 Pageable로 limit 제어
     */
    @Query("""
        SELECT w 
        FROM WithDrawnUser w 
        WHERE w.purgeStatus = :status AND w.purgeAfter <= :now 
        ORDER BY w.purgeAfter ASC, w.id ASC
    """)
    List<WithDrawnUser> findPurgeTargets(
            @Param("status") PurgeStatus status, @Param("now") LocalDateTime now, Pageable pageable
    );

    /**
     * 먼저 id만 뽑고 -> 자식 테이블 bulk delete -> status 업데이트 순으로 가는 방식도 고려
     */
    @Query("""
        SELECT w.id 
        FROM WithDrawnUser w 
        WHERE w.purgeStatus = :status AND w.purgeAfter <= :now 
        ORDER BY w.purgeAfter ASC, w.id ASC
    """)
    List<Long> findPurgeTargetIds(
            @Param("status") PurgeStatus status, @Param("now") LocalDateTime now, Pageable pageable
    );

    /**
     * 배치 처리 후 상태 업데이트 (성공)
     * - 벌크 업데이트라 영속성 컨텍스트 sync 안 됨
     * - 서비스에서 @Transactional + clear 고려
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WithDrawnUser w
        SET w.purgeStatus = :status,
            w.purgedAt = :purgedAt,
            w.failReason = null
        WHERE w.id IN :ids
    """)
    int markSuccessBulk(
            @Param("ids") List<Long> ids, @Param("status") PurgeStatus status, @Param("purgedAt") LocalDateTime purgedAt
    );

    /**
     * 배치 처리 후 상태 업데이트 (실패)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WithDrawnUser w
        SET w.purgeStatus = :status,
            w.purgedAt = null,
            w.failReason = :reason
        WHERE w.id = :id
    """)
    int markFailed(
            @Param("id") Long id, @Param("status") PurgeStatus status, @Param("reason") String reason
    );
}
