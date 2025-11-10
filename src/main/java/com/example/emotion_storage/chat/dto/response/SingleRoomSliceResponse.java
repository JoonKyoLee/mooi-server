package com.example.emotion_storage.chat.dto.response;

public record SingleRoomSliceResponse(
        RoomWithChatsDto roomWithChats,
        Long nextCursor,
        boolean hasNext
) {}
