package com.example.emotion_storage.chat.dto;

public record UserMessageDto(
        String messageId,
        String roomId,
        String content,
        String messageType,
        String timestamp
) {}
