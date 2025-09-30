package com.example.emotion_storage.mypage.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record NotificationSettingsResponse(
        boolean appPushNotify,
        boolean emotionReminderNotify,
        Set<DayOfWeek> emotionReminderDays,
        LocalTime emotionReminderTime,
        boolean timeCapsuleReportNotify,
        boolean marketingInfoNotify
) {}
