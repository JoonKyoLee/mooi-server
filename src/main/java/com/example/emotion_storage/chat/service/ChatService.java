package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.repository.ChatRoomRepository;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ApiResponse<ChatRoomCreateResponse> createChatRoom(CustomUserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(false)
                .build();

        chatRoomRepository.save(chatRoom);

        log.info("사용자 {}가 감정대화를 진행할 수 있는 채팅방 id {} 생성이 완료되었습니다.", user.getId(), chatRoom.getId());

        ChatRoomCreateResponse response = new ChatRoomCreateResponse(chatRoom.getId().toString());

        return ApiResponse.success(SuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage(), response);
    }
}
