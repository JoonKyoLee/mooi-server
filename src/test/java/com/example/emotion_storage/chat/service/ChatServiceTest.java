package com.example.emotion_storage.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.repository.ChatRoomRepository;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ChatServiceTest {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatService chatService;

    private User newUser() {
        return userRepository.save(User.builder()
                .socialType(SocialType.GOOGLE)
                .socialId("social123")
                .email("test@example.com")
                .profileImageUrl("http://example.com/profile.png")
                .nickname("tester")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2000,1,1))
                .keyCount(5L)
                .ticketCount(10L)
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isMarketingAgreed(false)
                .build());
    }

    private ChatRoom newChatRoom(User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(false)
                .isTempSave(false)
                .build();
        user.addChatRoom(chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    private ChatRoom tempSaveChatRoom(User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(false)
                .firstChatTime(LocalDateTime.now().minusSeconds(30))
                .isTempSave(true)
                .build();
        user.addChatRoom(chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    private ChatRoom finishedChatRoom(User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(true)
                .isTempSave(false)
                .firstChatTime(LocalDateTime.now().minusSeconds(30))
                .build();
        user.addChatRoom(chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    @Test
    void 감정_대화_기존_채팅방이_존재하지_않으면_채팅방_생성에_성공한다() {
        // given
        User user = newUser();
        Long userId = user.getId();

        // when
        ChatRoomCreateResponse response = chatService.createChatRoom(userId);

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.isTempSave()).isFalse();
        assertThat(response.isFirstChatOfDay()).isTrue();

        ChatRoom chatRoom = chatRoomRepository.findById(response.roomId())
                        .orElseThrow();
        assertThat(chatRoom.getUser().getId()).isEqualTo(userId);
        assertThat(chatRoom.isEnded()).isFalse();
    }

    @Test
    void 감정_대화_기존_채팅방이_존재하면_기존_채팅방을_반환한다() {
        // given
        User user = newUser();
        Long userId = user.getId();
        ChatRoom chatRoom = newChatRoom(user);

        // when
        ChatRoomCreateResponse response = chatService.createChatRoom(userId);

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.roomId()).isEqualTo(chatRoom.getId());
        assertThat(response.isTempSave()).isFalse();
        assertThat(response.isFirstChatOfDay()).isTrue();
        assertThat(chatRoom.getUser().getId()).isEqualTo(userId);
        assertThat(chatRoom.isEnded()).isFalse();
    }

    @Test
    void 감정_대화_임시저장_채팅방이_존재하면_임시저장_채팅방을_반환한다() {
        // given
        User user = newUser();
        Long userId = user.getId();
        ChatRoom chatRoom = tempSaveChatRoom(user);

        // when
        ChatRoomCreateResponse response = chatService.createChatRoom(userId);

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.roomId()).isEqualTo(chatRoom.getId());
        assertThat(response.isTempSave()).isTrue();
        assertThat(response.isFirstChatOfDay()).isTrue();
        assertThat(chatRoom.getUser().getId()).isEqualTo(userId);
        assertThat(chatRoom.isEnded()).isFalse();
    }

    @Test
    void 당일_대화한_채팅방이_존재하면_당일_첫_대화여부를_false로_반환한다() {
        // given
        User user = newUser();
        Long userId = user.getId();
        ChatRoom chatRoom = finishedChatRoom(user);

        // when
        ChatRoomCreateResponse response = chatService.createChatRoom(userId);

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.isTempSave()).isFalse();
        assertThat(response.isFirstChatOfDay()).isFalse();
    }

    @Test
    void 유저가_존재하지_않을_때_채팅방_개설_요청_시에_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> chatService.createChatRoom(9999L))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 감정_대화_채팅방_종료에_성공한다() {
        // given
        User user = newUser();
        Long userId = user.getId();

        ChatRoom chatRoom = newChatRoom(user);

        // when
        ChatRoomCloseResponse response = chatService.closeChatRoom(userId, chatRoom.getId());

        // then
        assertThat(response.finished()).isTrue();

        ChatRoom reloaded = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow();
        assertThat(reloaded.isEnded()).isTrue();
        assertThat(reloaded.getUser().getId()).isEqualTo(userId);
    }

    @Test
    void 채팅방이_존재하지_않을_때_채팅방_종료_호출_시에_예외가_발생한다() {
        // given
        User user = newUser();

        // when & then
        assertThatThrownBy(() -> chatService.closeChatRoom(user.getId(), 9999L))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());
    }

    @Test
    void 유저_아이디가_다를_때_채팅방_종료_호출_시에_예외가_발생한다() {
        // given
        User user = newUser();
        ChatRoom chatRoom = newChatRoom(user);

        // when & then
        assertThatThrownBy(() -> chatService.closeChatRoom(999L, chatRoom.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_ACCESS_DENIED.getMessage());
    }
}
