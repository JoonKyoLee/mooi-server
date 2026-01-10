package com.example.emotion_storage.chat.dto.response;

import com.example.emotion_storage.chat.domain.Chat;
import java.time.LocalDateTime;

public record ChatDto(
        Long id,
        String clientId,
        String sender,
        String message,
        LocalDateTime chatTime
) {
    public static ChatDto from(Chat chat) {
        return new ChatDto(
                chat.getId(), chat.getId().toString(), chat.getSender().name(), chat.getMessage(), chat.getChatTime()
        );
    }
}
