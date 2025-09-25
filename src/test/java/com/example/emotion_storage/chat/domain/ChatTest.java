package com.example.emotion_storage.chat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ChatTest {

    @Test
    void 채팅이_정상적으로_저장된다() {
        // given & when
        LocalDateTime now = LocalDateTime.now();

        Chat chat = Chat.builder()
                .chatRoom(null)
                .chatTime(now)
                .sender(SenderType.USER)
                .message("안녕하세요.")
                .build();

        // then
        assertThat(chat.getChatTime()).isEqualTo(now);
        assertThat(chat.getSender()).isEqualTo(SenderType.USER);
        assertThat(chat.getMessage()).isEqualTo("안녕하세요.");
    }
}
