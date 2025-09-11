package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.home.dto.response.KeyCountResponse;
import com.example.emotion_storage.home.dto.response.TicketStatusResponse;
import com.example.emotion_storage.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("emotion-conversation/test")
    @Operation(summary = "감정 대화 테스트를 위한 채팅방 반환 API", description = "감정 대화 테스트를 위한 채팅방 ID를 생성하고 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createTest() {
        ChatRoomCreateResponse response = new ChatRoomCreateResponse(UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage(), response));
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

}

