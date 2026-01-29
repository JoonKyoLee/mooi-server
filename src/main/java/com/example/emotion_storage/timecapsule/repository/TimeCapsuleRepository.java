package com.example.emotion_storage.timecapsule.repository;

import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeCapsuleRepository extends JpaRepository<TimeCapsule, Long> {

    Optional<TimeCapsule> findByChatroomIdAndIsTempSaveTrue(Long chatroomId);
    
    @Query("SELECT tc FROM TimeCapsule tc WHERE tc.user.id = :userId AND tc.isOpened = false AND tc.deletedAt IS NULL")
    List<TimeCapsule> findUnopenedTimeCapsulesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(tc) FROM TimeCapsule tc WHERE tc.user.id = :userId AND tc.isOpened = false AND tc.deletedAt IS NULL")
    Long countUnopenedTimeCapsulesByUserId(@Param("userId") Long userId);

    Optional<TimeCapsule> findTimeCapsuleByIdAndDeletedAtIsNull(Long id);

    @Query("""
        SELECT COUNT(tc)
        FROM TimeCapsule tc
        WHERE tc.user.id = :userId
          AND tc.deletedAt IS NULL
          AND tc.isTempSave = false
          AND tc.isOpened = false
          AND tc.openedAt >= :start
          AND tc.openedAt <= :end
    """)
    Long countUnopenedArrivedTimeCapsulesInRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT CAST(tc.historyDate AS DATE) from TimeCapsule tc "
            + "WHERE tc.user.id = :userId AND tc.historyDate >= :start AND tc.historyDate < :end AND tc.deletedAt IS NULL "
            + "ORDER BY CAST(tc.historyDate AS DATE)")
    List<Date> findActiveDatesInRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 전체 타임캡슐 목록
    Page<TimeCapsule> findByUser_IdAndDeletedAtIsNullAndIsOpenedFalseAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    // 도착한 타임캡슐 목록
    Page<TimeCapsule> findByUser_IdAndDeletedAtIsNullAndHistoryDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    // 즐겨찾기한 타임캡슐 목록
    Page<TimeCapsule> findByUser_IdAndDeletedAtIsNullAndIsFavoriteIsTrue(Long userId, Pageable pageable);

    int countByUser_IdAndIsFavoriteTrue(Long userId);

    // 전체 유저에 대해 도착한 타임캡슐 조회에 사용
    @Query("""
    SELECT tc from TimeCapsule tc JOIN tc.user u
    WHERE tc.deletedAt is null
    AND tc.isTempSave is false
    AND tc.isOpened is false
    AND tc.openedAt <= :now
    AND u.deletedAt is null
    """)
    Page<TimeCapsule> findArrivalTargetCapsules(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 특정 사용자의 특정 날짜에 생성된 타임캡슐 목록 조회
     * deletedAt이 null이고 임시 저장이 아닌 타임캡슐만 조회
     */
    @Query("SELECT tc FROM TimeCapsule tc " +
           "WHERE tc.user.id = :userId " +
           "AND tc.historyDate >= :startOfDay " +
           "AND tc.historyDate < :startOfNextDay " +
           "AND tc.deletedAt IS NULL " +
           "AND tc.isTempSave = false " +
           "ORDER BY tc.historyDate ASC")
    List<TimeCapsule> findByUserIdAndHistoryDate(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
    );
}
