package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final ChatService chatService;

    @PostMapping("emotion-conversation/test")
    @Operation(summary = "감정 대화 테스트를 위한 채팅방 반환 API", description = "감정 대화 테스트를 위한 채팅방 ID를 생성하고 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoomForTest() {
        ApiResponse<ChatRoomCreateResponse> response = chatService.createTestChatRoom();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/emotion-conversation/{roomId}")
    @Operation(summary = "테스트 채팅방 종료를 위한 API", description = "테스트용 감정 대화 채팅방을 종료합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCloseResponse>> closeTestChatRoom(@PathVariable String roomId) {
        log.info("채팅방 {}를 종료 상태로 업데이트 합니다.", roomId);
        ApiResponse<ChatRoomCloseResponse> response = chatService.closeTestChatRoom(roomId);
        return ResponseEntity.ok(response);
    }
}

