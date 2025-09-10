package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.dto.UserMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    public void chatTest(UserMessageDto userMessage) {
        String message = "안녕하세요, 저는 MOOI입니다. 메시지를 보내주세요.";
        messagingTemplate.convertAndSend("/sub/chatroom/" + userMessage.roomId(), message);
    }
}

