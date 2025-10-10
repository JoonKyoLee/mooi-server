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
}
