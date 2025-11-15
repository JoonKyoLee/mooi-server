package com.example.emotion_storage.attendance.response;

public record AttendanceStreakStatusResponse(
        int streak,
        boolean isAttendedToday
) {}
