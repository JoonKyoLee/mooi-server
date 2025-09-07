package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 관련 API")
public class HomeController {

    private final ChatService chatService;

    @PostMapping("/emotion-conversation")
    @Operation(summary = "감정 대화 시작", description = "감정 대화를 시작하기 위해 채팅방 id를 생성 후 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoom(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        log.info("사용자 {}의 감정 대화 시작을 위해 채팅방 개설을 진행합니다.", userPrincipal.getId());
        ApiResponse<ChatRoomCreateResponse> response = chatService.createChatRoom(userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
