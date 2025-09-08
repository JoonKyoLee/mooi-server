package com.example.emotion_storage.chat.controller;

import com.example.emotion_storage.chat.dto.UserMessageDto;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "StompChat", description = "웹소켓 STOMP 관련 API")
public class StompChatController {

    private final ChatService chatService;

    @MessageMapping("/v1/chat")
    public void processMessage(
            UserMessageDto userMessage,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        String userId = userPrincipal.getId().toString();
        log.info("사용자 {}가 채팅 메시지를 전송했습니다: {}", userId, userMessage.content());

        chatService.saveUserMessage(userMessage);

        // AI 메시지 프로세스 진행
        // ChatResponse response = chatService.....
        String message  = "hello";
        chatService.sendToUser(userMessage.roomId(), message);
    }
}
