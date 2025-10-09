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
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
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
import com.example.emotion_storage.chat.dto.response.AiChatResponse;
import com.example.emotion_storage.chat.dto.response.ChatMessageResponse;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // 메시지 관련 상수
    private static final String ERROR_MESSAGE_TEMPORARY = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    private static final String ERROR_MESSAGE_AI_COMMUNICATION = "AI 서버와의 통신 중 오류가 발생했습니다.";
    private static final String ERROR_MESSAGE_PROCESSING = "메시지 처리 중 오류가 발생했습니다.";
    private static final String INFO_MESSAGE_PROCESSING = "메시지를 받았습니다. AI가 응답을 준비 중입니다...";
    
    // 메시지 타입 상수
    private static final String MESSAGE_TYPE_ERROR = "chat.error";
    
    // WebSocket 엔드포인트 상수
    private static final String WEBSOCKET_DESTINATION_PREFIX = "/sub/chatroom/";
    
    // 세션 ID 포맷 상수
    private static final String SESSION_ID_FORMAT = "session-%d-%d";
    
    // 날짜 포맷 상수
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

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

        chatRoomRepository.save(chatRoom);

        log.info("사용자 {}가 감정대화를 진행할 수 있는 채팅방 id {} 생성이 완료되었습니다.", userId, chatRoom.getId());

        return new ChatRoomCreateResponse(chatRoom.getId());
    }

    @Transactional
    public void saveUserMessage(UserMessageDto userMessage) {
        ChatRoom chatRoom = chatRoomRepository.findById(userMessage.roomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

        // 첫 채팅 시각 기록
        if (chatRoom.getFirstChatTime() == null) {
            log.info("채팅방 {}의 첫 채팅시각을 기록합니다.", chatRoom.getId());
            LocalDateTime firstChatTime = LocalDateTime.parse(userMessage.timestamp(), formatter);
            chatRoom.setFirstChatTime(firstChatTime);
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

    public void sendToUser(Long roomId, String message) {
        messagingTemplate.convertAndSend(WEBSOCKET_DESTINATION_PREFIX + roomId, message);
        log.debug("채팅방 {}로 메시지 전송 완료", roomId);
    }

    public void sendToUser(Long roomId, ChatMessageResponse response) {
        messagingTemplate.convertAndSend(WEBSOCKET_DESTINATION_PREFIX + roomId, response);
        log.debug("채팅방 {}로 응답 전송 완료: sender={}, messageType={}",
                roomId, response.getSender(), response.getMessageType());
    }

    private String generateSessionId(Long userId, Long roomId) {
        return String.format(SESSION_ID_FORMAT, userId, roomId);
    }

    public void processUserMessageAsync(UserMessageDto userMessage, Long userId) {
        Long roomId = userMessage.roomId();
        
        // 1. 사용자 메시지 저장
        saveUserMessage(userMessage);
        log.info("[채팅방:{}] 사용자 메시지 저장 완료", roomId);

        // 2. AI 서버로 메시지 전송
        String sessionId = generateSessionId(userId, roomId);
        log.info("[채팅방:{}] 사용자 {}의 메시지를 AI 서버로 전송합니다: {}", roomId, userId, userMessage.content());

        AiMessageDto aiMessage = AiMessageDto.createChatStartMessage(
                sessionId,
                ChatPromptMessages.EMOTION_ANALYSIS.getMessage(),
                userMessage.content()
        );

        // 3, 4. 비동기로 AI 응답 처리 및 저장, 전송
        webSocketClientService.sendMessageToAI(aiMessage)
                .thenAccept(aiResponse -> {
                    log.info("[채팅방:{}] AI 서버로부터 응답을 받았습니다: {}", roomId, aiResponse.getResponse());

                    // AI 메시지 저장
                    try {
                        saveAiMessage(roomId, aiResponse.getResponse(), aiResponse.getTimestamp());
                    } catch (Exception e) {
                        log.error("[채팅방:{}] AI 메시지 저장 중 오류 발생", roomId, e);
                    }

                    ChatMessageResponse response = ChatMessageResponse.builder()
                            .content(aiResponse.getResponse())
                            .sender(SenderType.MOOI)
                            .roomId(roomId)
                            .sessionId(sessionId)
                            .timestamp(aiResponse.getTimestamp())
                            .gauge(aiResponse.getGauge())
                            .messageType(aiResponse.getType())
                            .build();

                    // WebSocket을 통해 클라이언트에게 AI 응답 전송
                    sendToUser(roomId, response);
                })
                .exceptionally(throwable -> {
                    log.error("[채팅방:{}] AI 서버 통신 중 오류 발생", roomId, throwable);

                    ChatMessageResponse errorResponse = ChatMessageResponse.builder()
                            .content(ERROR_MESSAGE_TEMPORARY)
                            .sender(SenderType.MOOI)
                            .roomId(roomId)
                            .sessionId(sessionId)
                            .timestamp(LocalDateTime.now().toString())
                            .messageType(MESSAGE_TYPE_ERROR)
                            .build();

                    sendToUser(roomId, errorResponse);

                    return null;
                });
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

    /**
     * REST API를 통한 동기 통신
     */
    public ApiResponse<AiChatResponse> sendMessage(ChatRequest request, Long userId) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : userId + "-" + UUID.randomUUID();

            AiMessageDto aiMessage = AiMessageDto.createChatStartMessage(
                    sessionId,
                    ChatPromptMessages.EMOTION_ANALYSIS.getMessage(),
                    request.getMessage()
            );

            log.info("사용자 {}의 메시지를 AI 서버로 전송합니다: {}", userId, request.getMessage());

            // AI 서버로 메시지 전송 및 응답 대기 (블로킹)
            AiResponseDto aiResponse = webSocketClientService.sendMessageToAI(aiMessage).get();

            // AI 응답을 클라이언트용 응답으로 변환
            AiChatResponse response = new AiChatResponse(
                    aiResponse.getResponse(),
                    sessionId,
                    aiResponse.getTimestamp(),
                    true
            );

            log.info("AI 서버로부터 응답을 받았습니다: {}", aiResponse.getResponse());

            return ApiResponse.success(SuccessMessage.CHAT_SUCCESS.getMessage(), response);

        } catch (Exception e) {
            log.error("채팅 메시지 처리 중 오류 발생", e);
            throw new RuntimeException(ERROR_MESSAGE_AI_COMMUNICATION, e);
        }
    }

    /**
     * REST API를 통한 비동기 통신
     */
    public ApiResponse<AiChatResponse> sendUserMessage(ChatRequest request, Long userId) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : userId + "-" + UUID.randomUUID();

            log.info("사용자 {}가 비동기 메시지를 전송했습니다: {}", userId, request.getMessage());

            // 사용자 메시지에 대한 즉시 응답 (AI 응답 없이)
            AiChatResponse response = new AiChatResponse(
                    INFO_MESSAGE_PROCESSING,
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
                        // WebSocket을 통해 클라이언트에게 실시간으로 응답 전송
                        // Note: REST API 방식에서는 WebSocket 전송 대상을 특정하기 어려움
                    })
                    .exceptionally(throwable -> {
                        log.error("사용자 {}의 AI 응답 처리 중 오류 발생", userId, throwable);
                        return null;
                    });

            return ApiResponse.success(SuccessMessage.CHAT_SUCCESS.getMessage(), response);

        } catch (Exception e) {
            log.error("사용자 메시지 처리 중 오류 발생", e);
            throw new RuntimeException(ERROR_MESSAGE_PROCESSING, e);
        }
    }
}
