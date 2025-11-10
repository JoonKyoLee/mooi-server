package com.example.emotion_storage.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RoomWithChatsDto(
        Long chatRoomId,
        LocalDateTime firstChatTime,
        List<ChatDto> chats
) {}
