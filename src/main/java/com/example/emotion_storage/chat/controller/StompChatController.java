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
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        // 사용자 ID 추출 (인증되지 않은 경우 개발 테스트용 ID 사용)
        Long userId = (userPrincipal != null && userPrincipal.getId() != null) ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        
        log.info("[채팅방:{}] 사용자 {}가 메시지를 전송했습니다: {}", 
                userMessage.roomId(), userId, userMessage.content());

        try {
            chatService.processUserMessageAsync(userMessage, userId);
        } catch (Exception e) {
            log.error("[채팅방:{}] 메시지 처리 중 오류 발생", userMessage.roomId(), e);
            
            // 오류 발생 시 클라이언트에게 에러 메시지 전송
            String errorMessage = "메시지 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
            chatService.sendToUser(userMessage.roomId(), errorMessage);
        }
    }
}
