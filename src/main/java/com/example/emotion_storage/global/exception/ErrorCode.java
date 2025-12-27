package com.example.emotion_storage.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NEED_SIGN_UP(HttpStatus.BAD_REQUEST, "회원가입이 필요합니다."),
    ALREADY_REGISTERED_WITH_GOOGLE(HttpStatus.BAD_REQUEST, "해당 이메일은 구글로 가입된 계정입니다."),
    ALREADY_REGISTERED_WITH_KAKAO(HttpStatus.BAD_REQUEST, "해당 이메일은 카카오로 가입된 계정입니다."),
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 ID TOKEN 입니다."),
    INVALID_KAKAO_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 Access Token 입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 Access Token 입니다."),
    ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token 입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token 입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방에 접근할 수 없습니다."),
    TICKET_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "티켓이 부족하여 대화를 시작할 수 없습니다."),

    TIME_CAPSULE_NOT_FOUND(HttpStatus.NOT_FOUND, "타임캡슐을 찾을 수 없습니다."),
    TIME_CAPSULE_IS_NOT_OWNED(HttpStatus.FORBIDDEN, "사용자의 타임캡슐이 아닙니다."),
    TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "즐겨찾기는 최대 30개까지만 가능합니다."),
    TIME_CAPSULE_KEY_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "열쇠가 부족하여 타임캡슐을 열 수 없습니다."),
    TIME_CAPSULE_OPEN_RULE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "열람 비용 규칙이 정의되어 있지 않습니다."),
    TIME_CAPSULE_NOT_TEMP_SAVE(HttpStatus.BAD_REQUEST, "임시저장된 타임캡슐이 아닙니다."),
    TIME_CAPSULE_DRAFT_EXPIRED(HttpStatus.GONE, "타임캡슐 임시저장 기간이 만료되었습니다."),
    TIME_CAPSULE_OPEN_DATE_BEFORE_STORED_AT(HttpStatus.BAD_REQUEST, "오픈일은 보관일 이전일 수 없습니다."),
    TIME_CAPSULE_OPEN_DATE_AFTER_LIMIT(HttpStatus.BAD_REQUEST, "오픈일은 보관일로부터 1년을 초과할 수 없습니다."),

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "리포트를 찾을 수 없습니다."),
    DAILY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "일일리포트 조회 실패 - 존재하지 않음"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다."),
    AI_SERVER_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버 요청에 실패했습니다."),
    AI_SERVER_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버 응답이 없습니다."),
    DAILY_REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "일일 리포트 생성에 실패했습니다."),

    EMOTION_REMINDER_DAYS_REQUIRED(HttpStatus.BAD_REQUEST, "감정 기록 알림 요일이 필요합니다."),
    EMOTION_REMINDER_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "감정 기록 알림 시간이 필요합니다."),

    ALREADY_GET_ATTENDANCE_REWARD(HttpStatus.BAD_REQUEST, "오늘 출석 보상을 이미 받았습니다."),
    EXPIRED_ATTENDANCE_REWARD(HttpStatus.BAD_REQUEST, "출석 보상을 받을 수 있는 기간이 지났습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
