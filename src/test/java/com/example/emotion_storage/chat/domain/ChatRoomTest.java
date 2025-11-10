package com.example.emotion_storage.chat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ChatRoomTest {

    private ChatRoom newChatRoom() {
        return ChatRoom.builder()
                .user(null)
                .isEnded(false)
                .firstChatTime(null)
                .build();
    }

    @Test
    void 채팅방이_정상적으로_생성된다() {
        // given & when
        ChatRoom chatRoom = newChatRoom();

        // then
        assertThat(chatRoom.isEnded()).isFalse();
        assertThat(chatRoom.getFirstChatTime()).isNull();
    }

    @Test
    void 채팅방을_정상적으로_종료한다() {
        // given
        ChatRoom chatRoom = newChatRoom();

        // when
        chatRoom.closeChatRoom();

        // then
        assertThat(chatRoom.isEnded()).isTrue();
        assertThat(chatRoom.isTempSave()).isFalse();
    }

    @Test
    void 채팅방_임시저장에_성공한다() {
        // given
        ChatRoom chatRoom = newChatRoom();

        // when
        chatRoom.tempSave();

        // then
        assertThat(chatRoom.isTempSave()).isTrue();
    }

    @Test
    void 첫_채팅_시각_기록에_성공한다() {
        // given
        ChatRoom chatRoom = newChatRoom();
        LocalDateTime now = LocalDateTime.now();

        // when
        chatRoom.setFirstChatTime(now);

        // then
        assertThat(chatRoom.getFirstChatTime()).isNotNull();
        assertThat(chatRoom.getFirstChatTime()).isEqualTo(now);
    }
}
