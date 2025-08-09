package com.example.emotion_storage.global.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessMessage {
    LOGIN_SUCCESS("로그인 성공"),
    SIGNUP_SUCCESS("회원가입 성공");

    private final String message;
}
