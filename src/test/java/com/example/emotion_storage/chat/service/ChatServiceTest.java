package com.example.emotion_storage.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.emotion_storage.chat.domain.Chat;
import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomTempSaveResponse;
import com.example.emotion_storage.chat.dto.response.SingleRoomSliceResponse;
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
    @Autowired private ChatRepository chatRepository;
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

    private User otherUser() {
        return userRepository.save(User.builder()
                .socialType(SocialType.GOOGLE)
                .socialId("social1234")
                .email("other@example.com")
                .profileImageUrl("http://example.com/profileImg.png")
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

    private ChatRoom createChatRoomWithFirstChatTime(User user, LocalDateTime time) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .isEnded(false)
                .isTempSave(false)
                .firstChatTime(time)
                .build();
        user.addChatRoom(chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    private Chat newChat(ChatRoom chatRoom, SenderType senderType, String message, LocalDateTime time) {
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .sender(senderType)
                .message(message)
                .chatTime(time)
                .build();
        return chatRepository.save(chat);
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
    void 감정_대화_채팅방_임시저장에_성공한다() {
        // given
        User user = newUser();
        ChatRoom chatRoom = newChatRoom(user);

        // when
        ChatRoomTempSaveResponse response = chatService.tempSave(user.getId(), chatRoom.getId());

        // then
        ChatRoom updated = chatRoomRepository.findById(response.chatRoomId())
                        .orElseThrow();

        assertThat(updated.isTempSave()).isTrue();
        assertThat(updated.getId()).isEqualTo(chatRoom.getId());
    }

    @Test
    void 채팅방이_존재하지_않을_때_임시_저장_로직_호출_시에_예외가_발생한다() {
        // given
        User user = newUser();

        // when & then
        assertThatThrownBy(() -> chatService.tempSave(user.getId(), 99999L))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());
    }

    @Test
    void 유저_아이디가_다를_때_임시_저장_호출_시에_예외가_발생한다() {
        // given
        User user = newUser();
        ChatRoom chatRoom = newChatRoom(user);

        // when & then
        assertThatThrownBy(() -> chatService.tempSave(9988L, chatRoom.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_ACCESS_DENIED.getMessage());
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

    @Test
    void 채팅방의_커서가_존재하지_않으면_가장_최신_채팅방의_모든_채팅을_반환한다() {
        // given
        User user = newUser();

        ChatRoom older = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(older, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(older, SenderType.MOOI, "오늘 기분은 어때?", LocalDateTime.now());
        newChat(older, SenderType.USER, "오늘은 좋았어", LocalDateTime.now());
        older.closeChatRoom();

        ChatRoom newer = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(newer, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(newer, SenderType.MOOI, "안녕, 오늘은 뭐했어?", LocalDateTime.now());
        newChat(newer, SenderType.USER, "오늘은 공부했어", LocalDateTime.now());
        newer.closeChatRoom();

        // when
        SingleRoomSliceResponse response = chatService.getMessagesInChatRoom(user.getId(), null);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.roomWithChats().chatRoomId()).isEqualTo(newer.getId());
        assertThat(response.roomWithChats().totalChatCount()).isEqualTo(3);
        assertThat(response.roomWithChats().chats().get(0).message()).isEqualTo("안녕");
        assertThat(response.roomWithChats().chats().get(1).message()).isEqualTo("안녕, 오늘은 뭐했어?");
        assertThat(response.roomWithChats().chats().get(2).message()).isEqualTo("오늘은 공부했어");
        assertThat(response.nextCursor()).isEqualTo(newer.getId());
    }

    @Test
    void 채팅방_커서가_존재하고_더이상_방이_없다면_hasNext가_false이다() {
        // given
        User user = newUser();

        ChatRoom older = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(older, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(older, SenderType.MOOI, "오늘 기분은 어때?", LocalDateTime.now());
        newChat(older, SenderType.USER, "오늘은 좋았어", LocalDateTime.now());
        older.closeChatRoom();

        ChatRoom newer = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(newer, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(newer, SenderType.MOOI, "안녕, 오늘은 뭐했어?", LocalDateTime.now());
        newChat(newer, SenderType.USER, "오늘은 공부했어", LocalDateTime.now());
        newer.closeChatRoom();

        // when
        SingleRoomSliceResponse response = chatService.getMessagesInChatRoom(user.getId(), newer.getId());

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.roomWithChats().chatRoomId()).isEqualTo(older.getId());
        assertThat(response.roomWithChats().totalChatCount()).isEqualTo(3);
        assertThat(response.roomWithChats().chats().get(0).message()).isEqualTo("안녕");
        assertThat(response.roomWithChats().chats().get(1).message()).isEqualTo("오늘 기분은 어때?");
        assertThat(response.roomWithChats().chats().get(2).message()).isEqualTo("오늘은 좋았어");
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void 채팅방이_없다면_비어있는_응답이_온다() {
        // given
        User user = newUser();

        // when
        SingleRoomSliceResponse response = chatService.getMessagesInChatRoom(user.getId(), null);

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.roomWithChats()).isNull();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void 채팅방_아이디가_연속이_아니더라도_응답이_잘_반환된다() {
        // given
        User user = newUser();
        User other = otherUser();

        ChatRoom older = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(older, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(older, SenderType.MOOI, "오늘 기분은 어때?", LocalDateTime.now());
        newChat(older, SenderType.USER, "오늘은 좋았어", LocalDateTime.now());
        older.closeChatRoom();

        ChatRoom otherChatRoom = createChatRoomWithFirstChatTime(other, LocalDateTime.now());

        ChatRoom newer = createChatRoomWithFirstChatTime(user, LocalDateTime.now());
        newChat(newer, SenderType.USER, "안녕", LocalDateTime.now());
        newChat(newer, SenderType.MOOI, "안녕, 오늘은 뭐했어?", LocalDateTime.now());
        newChat(newer, SenderType.USER, "오늘은 공부했어", LocalDateTime.now());
        newer.closeChatRoom();

        // when
        SingleRoomSliceResponse response = chatService.getMessagesInChatRoom(user.getId(), null);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.roomWithChats().chatRoomId()).isEqualTo(newer.getId());
        assertThat(response.roomWithChats().totalChatCount()).isEqualTo(3);
        assertThat(response.roomWithChats().chats().get(0).message()).isEqualTo("안녕");
        assertThat(response.roomWithChats().chats().get(1).message()).isEqualTo("안녕, 오늘은 뭐했어?");
        assertThat(response.roomWithChats().chats().get(2).message()).isEqualTo("오늘은 공부했어");
        assertThat(response.nextCursor()).isEqualTo(newer.getId());
    }
}
