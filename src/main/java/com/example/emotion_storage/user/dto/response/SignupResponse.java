package com.example.emotion_storage.user.dto.response;

public record SignupResponse(
        boolean success
) {
    public static SignupResponse ok() {
        return new SignupResponse(true);
    }
}
