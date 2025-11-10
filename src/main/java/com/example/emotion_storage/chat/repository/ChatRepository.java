package com.example.emotion_storage.chat.repository;

import com.example.emotion_storage.chat.domain.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(
            "SELECT c FROM Chat c " +
            "WHERE c.chatRoom.id = :roomId " +
            "ORDER BY c.chatTime ASC, c.id ASC "
    )
    List<Chat> findAllByRoomIdOrderByTimeAsc(@Param("roomId") Long roomId);
}
