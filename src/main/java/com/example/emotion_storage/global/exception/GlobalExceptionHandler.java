package com.example.emotion_storage.global.exception;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.monitoring.DiscordErrorNotifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordErrorNotifier discordErrorNotifier;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception, HttpServletRequest request) {
        log.error("[Exception]", exception);

        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = MDC.get("requestId");
        }

        discordErrorNotifier.notifyUnexpectedException(requestId, request, exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException baseException) {
        log.error("[BaseException]", baseException);
        ErrorCode errorCode = baseException.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode));
    }
}
