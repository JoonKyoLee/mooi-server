package com.example.emotion_storage.chat.controller;

import com.example.emotion_storage.chat.dto.request.ChatRequest;
import com.example.emotion_storage.chat.dto.response.AiChatResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomTempSaveResponse;
import com.example.emotion_storage.chat.dto.response.RoomWithChatsDto;
import com.example.emotion_storage.chat.dto.response.SingleRoomSliceResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "AI와 채팅 메시지 전송", description = "사용자 메시지를 AI 서버로 전송하고 응답을 받습니다.")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 채팅 메시지를 전송했습니다: {}", userId, request.getMessage());
        
        ApiResponse<AiChatResponse> response = chatService.sendMessage(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-async")
    @Operation(summary = "비동기 채팅 메시지 전송", description = "사용자 메시지를 비동기로 AI 서버에 전송합니다.")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessageAsync(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 비동기 채팅 메시지를 전송했습니다: {}", userId, request.getMessage());
        
        ApiResponse<AiChatResponse> response = chatService.sendUserMessage(request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{roomId}/temp-save")
    @Operation(summary = "채팅방 임시 저장", description = "채팅방을 임시 저장합니다.")
    public ResponseEntity<ApiResponse<ChatRoomTempSaveResponse>> tempSaveChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 채팅방 {}을 임시저장 요청했습니다.", userId, roomId);
        ChatRoomTempSaveResponse response = chatService.tempSave(userId, roomId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.CHAT_ROOM_TEMP_SAVE_SUCCESS.getMessage(), response)
        );
    }

    @GetMapping("/rooms")
    @Operation(summary = "하나의 채팅방의 모든 메시지 조회", description = "cursor가 없으면 최신 방부터, 있으면 해당 ID보다 이전 방을 반환합니다.")
    public ResponseEntity<ApiResponse<SingleRoomSliceResponse>> getMessagesInChatRoom(
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        SingleRoomSliceResponse response = chatService.getMessagesInChatRoom(userId, cursor);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.CHAT_ROOM_MESSAGE_FETCH_SUCCESS.getMessage(), response)
        );
    }
}
