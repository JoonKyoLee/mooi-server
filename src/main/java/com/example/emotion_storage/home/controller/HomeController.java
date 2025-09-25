package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.home.dto.response.HomeInfoResponse;
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

    @PostMapping("/emotion-conversation")
    @Operation(summary = "감정 대화 시작", description = "감정 대화를 시작하기 위해 채팅방 id를 생성 후 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoom(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 감정 대화 시작을 위해 채팅방 개설을 진행합니다.", userId);
        ChatRoomCreateResponse response = chatService.createChatRoom(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage(), response));
    }

    @DeleteMapping("/emotion-conversation/{roomId}")
    @Operation(summary = "감정 대화 종료", description = "감정 대화 채팅방을 종료합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCloseResponse>> closeChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 감정 대화를 종료하고 채팅방 {}를 종료 상태로 업데이트 합니다.", userId, roomId);
        ChatRoomCloseResponse response = chatService.closeChatRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessage.CHAT_ROOM_CLOSE_SUCCESS.getMessage(), response));
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
