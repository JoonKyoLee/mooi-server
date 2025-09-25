package com.example.emotion_storage.chat.dto;

public record UserMessageDto(
        String messageId,
        Long roomId,
        String content,
        String messageType,
        String timestamp
) {}
