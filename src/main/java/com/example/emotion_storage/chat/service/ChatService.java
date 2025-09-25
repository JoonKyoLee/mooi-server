package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.domain.Chat;
import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.UserMessageDto;
import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public ChatRoomCreateResponse createChatRoom(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(false)
                .build();
        user.addChatRoom(chatRoom);

        chatRoomRepository.save(chatRoom);

        log.info("사용자 {}가 감정대화를 진행할 수 있는 채팅방 id {} 생성이 완료되었습니다.", userId, chatRoom.getId());

        return new ChatRoomCreateResponse(chatRoom.getId());
    }

    @Transactional
    public void saveUserMessage(UserMessageDto userMessage) {
        ChatRoom chatRoom = chatRoomRepository.findById(userMessage.roomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"); // 프론트 포맷에 맞춰 변경 필요

        if (chatRoom.getFirstChatTime() == null) {
            log.info("채팅방 {}의 첫 채팅시각을 기록합니다.", chatRoom.getId());
            LocalDateTime firstChatTime = LocalDateTime.parse(userMessage.timestamp(), formatter);
            chatRoom.setFirstChatTime(firstChatTime);
        }

        log.info("채팅방 {}에 전송된 채팅을 저장합니다.", userMessage.roomId());
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .message(userMessage.content())
                .sender(SenderType.USER)
                .chatTime(LocalDateTime.parse(userMessage.timestamp(), formatter))
                .build();

        chatRoom.addChat(chat);
        chatRepository.save(chat);
    }

    @Transactional
    public ChatRoomCloseResponse closeChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        log.info("채팅방 {}에서 감정 대화 종료를 요청했습니다.", roomId);
        chatRoom.closeChatRoom(true);

        return new ChatRoomCloseResponse(true);
    }

    public void sendToUser(Long roomId, String message) { // 추루에 AI 메시지 DTO 형식으로 변경
        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId, message);
        // messagingTemplate.convertAndSend("sub/chatroom/" + roomId, AiMessageDto);
    }
}
