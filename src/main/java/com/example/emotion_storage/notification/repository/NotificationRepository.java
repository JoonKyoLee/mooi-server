package com.example.emotion_storage.notification.repository;

import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.notification.domain.NotificationType;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.deletedAt IS NULL")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.deletedAt IS NULL")
    Long countUnreadNotificationsByUserId(@Param("userId") Long userId);

    Page<Notification> findByUser_IdAndArrivedAtAfter(Long userId, LocalDateTime start, Pageable pageable);

    boolean existsByUser_IdAndTypeAndTargetId(Long userId, NotificationType type, Long targetId);

    long countByUser_IdAndType(Long userId, NotificationType type);

    long countByUser_IdAndTypeAndTargetId(Long userId, NotificationType type, Long targetId);
}
