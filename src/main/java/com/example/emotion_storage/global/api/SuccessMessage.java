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

    // Chat
    CHAT_ROOM_CREATE_SUCCESS("대화 시작 성공"),
    CHAT_ROOM_CLOSE_SUCCESS("대화 종료 성공"),
    CHAT_SUCCESS("채팅 메시지 전송 성공"),

    // Home
    GET_TICKETS_SUCCESS("티켓 개수 조회 성공"),
    GET_KEYS_SUCCESS("열쇠 개수 조회 성공"),
    GET_NEW_TIME_CAPSULE_SUCCESS("도착한 타임캡슐 여부 조회 성공"),
    GET_NEW_DAILY_REPORT_SUCCESS("새로운 일일리포트 여부 조회 성공"),
    GET_NEW_NOTIFICATION_SUCCESS("신규 알림 여부 조회 성공"),
    GET_HOME_INFO_SUCCESS("홈 화면 정보 API 조회 성공"),

    // Time Capsule
    GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS("월별 타임캡슐 날짜 목록 조회 성공"),
    GET_TIME_CAPSULE_LIST_SUCCESS("타임캡슐 목록 조회 성공"),
    GET_FAVORITE_TIME_CAPSULE_LIST_SUCCESS("즐겨찾기 타임캡슐 목록 조회 성공"),
    ADD_FAVORITE_TIME_CAPSULE_SUCCESS("타임캡슐 즐겨찾기 추가 성공"),
    REMOVE_FAVORITE_TIME_CAPSULE_SUCCESS("타임캡슐 즐겨찾기 해제 성공"),
    GET_TIME_CAPSULE_DETAIL_SUCCESS("타임캡슐 상세 조회 성공"),
    OPEN_TIME_CAPSULE_SUCCESS("타임캡슐 열람 성공"),
    UPDATE_TIME_CAPSULE_MIND_NOTE_SUCCESS("타임캡슐 내 마음 노트 수정 성공"),
    DELETE_TIME_CAPSULE_SUCCESS("타임캡슐 삭제 성공"),
    CREATE_TIME_CAPSULE_SUCCESS("타임캡슐 생성 성공"),
    
    // Report
    DAILY_REPORT_DETAIL_GET_SUCCESS("일일리포트 상세 조회 성공"),

    // My Page
    GET_MY_PAGE_USER_INFO_SUCCESS("마이페이지 내 사용자 정보 조회 성공"),
    CHANGE_USER_NICKNAME_SUCCESS("사용자 닉네임 업데이트 성공"),
    GET_USER_ACCOUNT_INFO_SUCCESS("사용자 계정 정보 조회 성공"),
    GET_NOTIFICATION_SETTINGS_SUCCESS("사용자 알림 설정 상태 정보 조회 성공"),
    UPDATE_NOTIFICATION_SETTINGS_SUCCESS("사용자 알림 설정 상태 정보 업데이트 성공"),
    GET_POLICY_SUCCESS("이용 약관 및 개인정보 처리방침 조회 성공"),
    WITHDRAW_USER_SUCCESS("회원 탈퇴 처리 성공"),
    LOGOUT_USER_SUCCESS("회원 로그아웃 처리 성공"),
    SENTIMENT_ANALYSIS_SUCCESS("감정 분석 성공"),
    GET_USER_KEY_COUNT_SUCCESS("회원 열쇠 개수 조회 성공"),
    ;

    private final String message;
}
