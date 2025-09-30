package com.example.emotion_storage.mypage.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record NotificationSettingsUpdateRequest(
        boolean appPushNotify,
        boolean emotionReminderNotify,
        Set<DayOfWeek> emotionReminderDays,
        LocalTime emotionReminderTime,
        boolean timeCapsuleReportNotify,
        boolean marketingInfoNotify
) {}
