package com.example.emotion_storage.chat.service;

public final class SentimentAnalysisPrompts {

    public static final String ROLE_MESSAGE = "너는 전문 심리 상담사야.";

    public static final String REFERENCE_MESSAGE = "다음은 오늘 하루 동안 사람과 상담봇이 나눈 복수의 대화 기록이야.";

    public static final String ANALYZE_MESSAGE = "이 대화를 바탕으로 다음과 같은 항목들을 알아 내야 해\n\n" +
            "- 각 대화로부터 유추한 오늘 있었던 일 요약 리스트 (예: [\"대화 1 요약\", \"대화 2 요약\"])\n" +
            "- 오늘을 대표하는 주요 키워드 (3~5개 단어)\n" +
            "- 감정 변화 흐름 요약 (예: [\"아침에 무기력함\", \"오후에 분노\", \"저녁에 안정\"])\n" +
            "- 0~100 사이의 정수로 표현된 스트레스 수준\n" +
            "- 0~100 사이의 정수로 표현된 행복 수준\n" +
            "- 오늘 하루 감정 전반에 대해 종합적으로 평가한 짧은 문장";

    private SentimentAnalysisPrompts() {
    }
}


