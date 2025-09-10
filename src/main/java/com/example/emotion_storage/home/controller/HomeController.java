package com.example.emotion_storage.home.controller;

import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 관련 API")
public class HomeController {

    @PostMapping("emotion-conversation/test")
    @Operation(summary = "감정 대화 테스트를 위한 채팅방 반환 API", description = "감정 대화 테스트를 위한 채팅방 ID를 생성하고 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createTest() {
        ChatRoomCreateResponse response = new ChatRoomCreateResponse(UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage(), response));
    }
}

