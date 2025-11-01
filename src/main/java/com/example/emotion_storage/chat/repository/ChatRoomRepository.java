package com.example.emotion_storage.chat.repository;

import com.example.emotion_storage.chat.domain.ChatRoom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query(
            "SELECT cr from ChatRoom cr " +
            "WHERE cr.user.id = :userId " +
            "AND (cr.createdAt < :createdAt or (cr.createdAt = :createdAt AND cr.id < :currentChatRoomId)) " +
            "ORDER BY cr.createdAt DESC, cr.id DESC"
    )
    Optional<ChatRoom> findPrevRoom(Long userId, LocalDateTime createdAt, Long currentChatRoomId);
}
