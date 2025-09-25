package com.example.emotion_storage.home.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.emotion_storage.chat.dto.response.ChatRoomCloseResponse;
import com.example.emotion_storage.chat.dto.response.ChatRoomCreateResponse;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.config.TestSecurityConfig;
import com.example.emotion_storage.home.service.HomeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
public class HomeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ChatService chatService;
    @MockitoBean HomeService homeService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 감정_대화_시작_시에_채팅방_ID를_반환한다() throws Exception {
        // given
        given(chatService.createChatRoom(anyLong())).willReturn(new ChatRoomCreateResponse(1L));

        // when & then
        mockMvc.perform(post("/api/v1/home/emotion-conversation"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roomId").value(1L))
                .andExpect(jsonPath("$.message").value(SuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage()));
    }

    @Test
    void 감정_대화_종료에_성공한다() throws Exception {
        // given
        Long roomId = 1L;
        given(chatService.closeChatRoom(1L, roomId)).willReturn(new ChatRoomCloseResponse(true));

        // when & then
        mockMvc.perform(delete("/api/v1/home/emotion-conversation/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finished").value(true))
                .andExpect(jsonPath("$.message").value(SuccessMessage.CHAT_ROOM_CLOSE_SUCCESS.getMessage()));
    }
}
