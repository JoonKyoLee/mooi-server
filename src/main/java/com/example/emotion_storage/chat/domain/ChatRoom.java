package com.example.emotion_storage.chat.domain;

import com.example.emotion_storage.global.entity.BaseTimeEntity;
import com.example.emotion_storage.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_ended", nullable = false)
    private boolean isEnded;

    @Column(name = "first_chat_time")
    private LocalDateTime firstChatTime;

    public void updateChatRoomStatus(boolean isEnded) {
        this.isEnded = isEnded;
    }

    public void setFirstChatTime(LocalDateTime firstChatTime) {
        this.firstChatTime = firstChatTime;
    }
}
