package com.example.emotion_storage.user.auth.dto.response;

public record SessionResponse(
        boolean success
) {
    public static SessionResponse ok() {
        return new SessionResponse(true);
    }
}
