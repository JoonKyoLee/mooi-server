package com.example.emotion_storage.notification.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.notification.dto.response.NotificationListResponse;
import com.example.emotion_storage.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회 API", description = "알림 목록(타임캡슐 및 일일리포트 도착)을 반환합니다.")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotificationList(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 알림 목록 조회(page: {}, limit: {})를 요청받았습니다.", userId, page, limit);
        NotificationListResponse response = notificationService.fetchNotifications(page, limit, userId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.GET_NOTIFICATION_LIST_SUCCESS.getMessage(), response)
        );
    }
}
