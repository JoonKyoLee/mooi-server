package com.example.emotion_storage.notification.domain;

public enum NotificationType {
    RECORD_SCHEDULE("오늘 어떻게 보냈어요?💬", "오늘 있었던 일, 아무거나 들어줄게요."),
    DAILY_REPORT_ARRIVAL("어제의 나, 리포트로 돌아왔어요📮", "하루의 마음 여정을 한눈에 만나보세요."),
    TIME_CAPSULE_ARRIVAL("기다리던 타임캡슐 도착!💌", "잠들어 있던 감정이 깨어났어요."),
    RECORD_REMINDER("우리 못본지 오래된 것 같아요...🥲", "%s님의 안부가 궁금해요.")
    ;

    private final String title;
    private final String content;

    NotificationType(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String title() { return title; }
    public String content() { return content; }
}
