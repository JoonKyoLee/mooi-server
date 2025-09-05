package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.domain.Chat;
import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.ChatPromptMessages;
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
import com.example.emotion_storage.chat.dto.AiMessageDto;
import com.example.emotion_storage.chat.dto.AiResponseDto;
import com.example.emotion_storage.chat.dto.request.ChatRequest;
import com.example.emotion_storage.chat.dto.response.ChatResponse;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final WebSocketClientService webSocketClientService;


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

    public ApiResponse<ChatResponse> sendMessage(ChatRequest request, Long userId) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : userId + "-" + UUID.randomUUID();

            AiMessageDto aiMessage = AiMessageDto.createChatStartMessage(
                    sessionId,
                    ChatPromptMessages.EMOTION_ANALYSIS.getMessage(),
                    request.getMessage()
            );

            log.info("사용자 {}의 메시지를 AI 서버로 전송합니다: {}", userId, request.getMessage());

            // AI 서버로 메시지 전송 및 응답 대기
            AiResponseDto aiResponse = webSocketClientService.sendMessageToAI(aiMessage).get();

            // AI 응답을 클라이언트용 응답으로 변환
            ChatResponse response = new ChatResponse(
                    aiResponse.getResponse(),
                    sessionId,
                    aiResponse.getTimestamp(),
                    true
            );

            log.info("AI 서버로부터 응답을 받았습니다: {}", aiResponse.getResponse());

            return ApiResponse.success(SuccessMessage.CHAT_SUCCESS.getMessage(), response);

        } catch (Exception e) {
            log.error("채팅 메시지 처리 중 오류 발생", e);
            throw new RuntimeException("AI 서버와의 통신 중 오류가 발생했습니다.", e);
        }
    }

    public ApiResponse<ChatResponse> sendUserMessage(ChatRequest request, Long userId) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : userId + "-" + UUID.randomUUID();

            log.info("사용자 {}가 비동기 메시지를 전송했습니다: {}", userId, request.getMessage());

            // 사용자 메시지에 대한 즉시 응답 (AI 응답 없이)
            ChatResponse response = new ChatResponse(
                    "메시지를 받았습니다. AI가 응답을 준비 중입니다...",
                    sessionId,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    false
            );

            // 백그라운드에서 AI 서버로 메시지 전송
            AiMessageDto aiMessage = AiMessageDto.createChatStartMessage(
                    sessionId,
                    ChatPromptMessages.EMOTION_ANALYSIS.getMessage(),
                    request.getMessage()
            );

            // 비동기로 AI 응답 처리
            webSocketClientService.sendMessageToAI(aiMessage)
                    .thenAccept(aiResponse -> {
                        log.info("사용자 {}의 AI 응답을 받았습니다: {}", userId, aiResponse.getResponse());
                        // 여기서 WebSocket을 통해 클라이언트에게 실시간으로 응답 전송
                        // TODO: 실시간 전송

                    })
                    .exceptionally(throwable -> {
                        log.error("사용자 {}의 AI 응답 처리 중 오류 발생", userId, throwable);
                        return null;
                    });

            return ApiResponse.success(SuccessMessage.CHAT_SUCCESS.getMessage(), response);

        } catch (Exception e) {
            log.error("사용자 메시지 처리 중 오류 발생", e);
            throw new RuntimeException("메시지 처리 중 오류가 발생했습니다.", e);
        }
    }
}
