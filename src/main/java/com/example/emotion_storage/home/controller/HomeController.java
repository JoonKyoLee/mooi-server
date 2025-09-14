package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.home.dto.response.HomeInfoResponse;
import com.example.emotion_storage.home.dto.response.KeyCountResponse;
import com.example.emotion_storage.home.dto.response.NewDailyReportResponse;
import com.example.emotion_storage.home.dto.response.NewNotificationResponse;
import com.example.emotion_storage.home.dto.response.NewTimeCapsuleResponse;
import com.example.emotion_storage.home.dto.response.TicketStatusResponse;
import com.example.emotion_storage.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 관련 API")
public class HomeController {

    private final HomeService homeService;
    private final ChatService chatService;

    @PostMapping("emotion-conversation/test")
    @Operation(summary = "감정 대화 테스트를 위한 채팅방 반환 API", description = "감정 대화 테스트를 위한 채팅방 ID를 생성하고 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoomForTest(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        ApiResponse<ChatRoomCreateResponse> response = chatService.createTestChatRoom(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/emotion-conversation/{roomId}")
    @Operation(summary = "테스트 채팅방 종료를 위한 API", description = "테스트용 감정 대화 채팅방을 종료합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCloseResponse>> closeTestChatRoom(@PathVariable String roomId) {
        log.info("채팅방 {}를 종료 상태로 업데이트 합니다.", roomId);
        ApiResponse<ChatRoomCloseResponse> response = chatService.closeTestChatRoom(roomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets")
    @Operation(summary = "티켓 상태(잔여 개수) 조회", description = "사용자가 가지고 있는 티켓 개수를 반환합니다.")
    public ResponseEntity<ApiResponse<TicketStatusResponse>> getTickets(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_TICKETS_SUCCESS.getMessage(), homeService.getTicketStatus(userId)));
    }

    @GetMapping("/keys")
    @Operation(summary = "열쇠 개수 조회", description = "사용자가 가지고 있는 열쇠 개수를 반환합니다.")
    public ResponseEntity<ApiResponse<KeyCountResponse>> getKeys(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_KEYS_SUCCESS.getMessage(), homeService.getKeyCount(userId)));
    }

    @GetMapping("/time-capsules")
    @Operation(summary = "도착한 타임캡슐 여부 조회", description = "사용자의 도착한 미확인 타임캡슐이 있는지 여부와 개수를 반환합니다.")
    public ResponseEntity<ApiResponse<NewTimeCapsuleResponse>> getNewTimeCapsules(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_NEW_TIME_CAPSULE_SUCCESS.getMessage(), 
                        homeService.getNewTimeCapsuleStatus(userId)));
    }

    @GetMapping("/daily-reports")
    @Operation(summary = "새로운 일일리포트 여부 조회", description = "사용자의 열어보지 않은 일일리포트가 있는지 여부와 개수를 반환합니다.")
    public ResponseEntity<ApiResponse<NewDailyReportResponse>> getNewDailyReports(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_NEW_DAILY_REPORT_SUCCESS.getMessage(), 
                        homeService.getNewDailyReportStatus(userId)));
    }

    @GetMapping("/notifications")
    @Operation(summary = "새로운 알림 여부 조회", description = "사용자의 미열람 알림이 있는지 여부와 개수를 반환합니다.")
    public ResponseEntity<ApiResponse<NewNotificationResponse>> getNewNotifications(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_NEW_NOTIFICATION_SUCCESS.getMessage(), 
                        homeService.getNewNotificationStatus(userId)));
    }

    @GetMapping("")
    @Operation(summary = "홈 정보 통합 조회", description = "홈 화면에 필요한 모든 정보(티켓, 열쇠, 알림, 타임캡슐, 리포트)를 한 번에 조회합니다.")
    public ResponseEntity<ApiResponse<HomeInfoResponse>> getHomeInfo(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.GET_HOME_INFO_SUCCESS.getMessage(), 
                        homeService.getHomeInfo(userId)));
    }

}

