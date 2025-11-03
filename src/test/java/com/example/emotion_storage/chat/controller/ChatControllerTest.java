package com.example.emotion_storage.chat.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.chat.dto.response.ChatRoomTempSaveResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
@Import(TestSecurityConfig.class)
public class ChatControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ChatService chatService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 채팅방_임시저장에_성공한다() throws Exception {
        // given
        Long roomId = 1L;

        ChatRoomTempSaveResponse response = new ChatRoomTempSaveResponse(1L);

        given(chatService.tempSave(anyLong(), eq(roomId)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/chat/{roomId}/temp-save", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.CHAT_ROOM_TEMP_SAVE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.chatRoomId").value(1L));
    }
}
