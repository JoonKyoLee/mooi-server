package com.example.emotion_storage.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docs/ws")
@Tag(name = "StompChat 관련 설명 컨트롤러", description = "웹소켓을 STOMP로 구현해 관련 기능을 설명합니다.")
public class WebSocketStompDocsController {

    @Operation(
            summary = "STOMP WebSocket 사용법",
            description = """
                          1. 연결: ws://호스트주소:8080/ws
                          2. 메시지 전송(publish): /pub/v1/test(테스트용)
                          3. 메시지 구독(subscribe): /sub/chatroom/{roomId}
                          클라이언트는 위 경로를 사용해 STOMP 프로토콜로 연결, 송신, 수신을 수행합니다.
                          """
    )
    @GetMapping
    public void doc() {}
}
