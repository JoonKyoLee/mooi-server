package com.example.emotion_storage.chat.dto.response;

import com.example.emotion_storage.chat.domain.SenderType;
import com.example.emotion_storage.chat.dto.GaugeDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    
    private String content;

    private SenderType sender;

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("session_id")
    private String sessionId;

    private String timestamp;

    private GaugeDto gauge;

    @JsonProperty("message_type")
    private String messageType;
}

