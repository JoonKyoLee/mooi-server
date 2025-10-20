package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.domain.Chat;
import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.UserMessageDto;
import com.example.emotion_storage.chat.repository.ChatRepository;
import com.example.emotion_storage.chat.repository.ChatRoomRepository;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageStore {

    // 날짜 포맷 상수
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveUserMessage(UserMessageDto userMessage, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(userMessage.roomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

        // 첫 채팅 시각 기록 및 티켓 차감
        if (chatRoom.getFirstChatTime() == null) {
            log.info("채팅방 {}의 첫 채팅시각을 기록합니다.", chatRoom.getId());
            LocalDateTime firstChatTime = LocalDateTime.parse(userMessage.timestamp(), formatter);
            chatRoom.setFirstChatTime(firstChatTime);

            log.info("대화를 시작하여 사용자 {}의 티켓을 차감합니다.", userId);
            user.useTicket();
        }

        log.info("채팅방 {}에 전송된 사용자 메시지를 저장합니다.", userMessage.roomId());
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .message(userMessage.content())
                .sender(SenderType.USER)
                .chatTime(LocalDateTime.parse(userMessage.timestamp(), formatter))
                .build();

        chatRepository.save(chat);

        log.debug("채팅방 {}에 사용자 메시지 저장 완료", userMessage.roomId());
    }

    @Transactional
    public void saveAiMessage(Long roomId, String message, String timestamp) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // AI 서버에서 받은 timestamp는 ISO 형식 (예: 2025-10-10T00:40:02.230468)
        LocalDateTime chatTime = LocalDateTime.parse(timestamp);

        log.info("채팅방 {}에 전송된 AI 메시지를 저장합니다.", roomId);
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .message(message)
                .sender(SenderType.MOOI)
                .chatTime(chatTime)
                .build();

        chatRepository.save(chat);

        log.debug("채팅방 {}에 AI 메시지 저장 완료", roomId);
    }
}
