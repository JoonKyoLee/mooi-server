package com.example.emotion_storage.global.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessMessage {
    LOGIN_SUCCESS("로그인 성공"),
    SIGNUP_SUCCESS("회원가입 성공"),
    ACCESS_TOKEN_REISSUE_SUCCESS("액세스 토큰 재발급 성공"),
    SESSION_CHECK_SUCCESS("세션 확인 성공"),

    CHAT_ROOM_CREATE_SUCCESS("대화 시작 성공"),

    // Home
    GET_TICKETS_SUCCESS("티켓 개수 조회 성공");

    private final String message;
}
