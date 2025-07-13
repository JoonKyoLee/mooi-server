package com.example.emotion_storage.global.exception;

public record ErrorResponse (
        int status,
        String message
) {
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getMessage());
    }
}
