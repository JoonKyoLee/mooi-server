package com.example.emotion_storage.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;

import com.example.emotion_storage.chat.domain.Chat;
import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.UserMessageDto;
import com.example.emotion_storage.chat.repository.ChatRepository;
import com.example.emotion_storage.chat.repository.ChatRoomRepository;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ChatMessageStoreTest {

    @Autowired private UserRepository userRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private ChatMessageStore chatMessageStore;

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
                .build();
        user.addChatRoom(chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    @Test
    void 유저_메시지_저장에_성공한다() {
        // given
        User user = newUser();
        ChatRoom chatRoom = newChatRoom(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = formatter.format(now);

        UserMessageDto userMessageDto = new UserMessageDto(
                "message-1",
                chatRoom.getId(),
                "안녕하세요",
                "USER",
                timestamp
        );

        // when
        chatMessageStore.saveUserMessage(userMessageDto, user.getId());

        // then
        ChatRoom reloaded = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow();
        assertThat(reloaded.getFirstChatTime()).isEqualTo(LocalDateTime.parse(timestamp, formatter));

        List<Chat> chats = chatRepository.findAll();
        assertThat(chats.get(0).getChatRoom().getId()).isEqualTo(chatRoom.getId());
        assertThat(chats.get(0).getMessage()).isEqualTo("안녕하세요");
        assertThat(chats.get(0).getSender()).isEqualTo(SenderType.USER);
        assertThat(chats.get(0).getChatTime()).isEqualTo(LocalDateTime.parse(timestamp, formatter));
    }

    @Test
    void 첫_메시지_이후_다시_저장해도_첫_채팅_시각은_변경되지_않는다() {
        // given
        User user = newUser();
        ChatRoom chatRoom = newChatRoom(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime firstTime = LocalDateTime.now().minusMinutes(1);
        LocalDateTime secondTime = LocalDateTime.now();
        String firstTimestamp = formatter.format(firstTime);
        String secondTimestamp = formatter.format(secondTime);

        UserMessageDto firstMessage = new UserMessageDto(
                "message-1",
                chatRoom.getId(),
                "메시지 1",
                "USER",
                firstTimestamp
        );
        UserMessageDto secondMessage = new UserMessageDto(
                "message-2",
                chatRoom.getId(),
                "메시지 2",
                "USER",
                secondTimestamp
        );

        // when
        chatMessageStore.saveUserMessage(firstMessage, user.getId());
        LocalDateTime firstChatTime = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow()
                .getFirstChatTime();

        chatMessageStore.saveUserMessage(secondMessage, user.getId());

        // then
        ChatRoom reloaded = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow();
        assertThat(reloaded.getFirstChatTime()).isEqualTo(firstChatTime);

        List<Chat> chats = chatRepository.findAll();
        assertThat(chats).hasSize(2);
        assertThat(chats.get(0).getMessage()).isEqualTo("메시지 1");
        assertThat(chats.get(1).getMessage()).isEqualTo("메시지 2");
    }

    @Test
    void 존재하지_않는_채팅방이면_유저_메시지_저장_시_예외가_발생한다() {
        // given
        User user = newUser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = formatter.format(now);

        UserMessageDto userMessageDto = new UserMessageDto(
                "message-1",
                9999L, // 존재하지 않는 roomId
                "안녕하세요",
                "USER",
                timestamp
        );

        // when & then
        assertThatThrownBy(() -> chatMessageStore.saveUserMessage(userMessageDto, user.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());
    }
}
