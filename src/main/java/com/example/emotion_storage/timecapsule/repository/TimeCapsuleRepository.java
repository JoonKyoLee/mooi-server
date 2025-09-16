package com.example.emotion_storage.timecapsule.repository;

import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeCapsuleRepository extends JpaRepository<TimeCapsule, Long> {
    
    @Query("SELECT tc FROM TimeCapsule tc WHERE tc.user.id = :userId AND tc.isOpened = false AND tc.deletedAt IS NULL")
    List<TimeCapsule> findUnopenedTimeCapsulesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(tc) FROM TimeCapsule tc WHERE tc.user.id = :userId AND tc.isOpened = false AND tc.deletedAt IS NULL")
    Long countUnopenedTimeCapsulesByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT CAST(tc.historyDate AS DATE) from TimeCapsule tc "
            + "WHERE tc.user.id = :userId AND tc.historyDate >= :start AND tc.historyDate < :end "
            + "ORDER BY CAST(tc.historyDate AS DATE)")
    List<LocalDate> findActiveDatesInRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
