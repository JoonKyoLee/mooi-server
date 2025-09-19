package com.example.emotion_storage.timecapsule.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeCapsuleStatus {
    TEMP_SAVED("임시 저장"), LOCKED("잠김"), ARRIVED("도착"), OPENED("열림");

    private final String statusMessage;

    public static TimeCapsuleStatus getStatus(TimeCapsule timeCapsule) {
        // 임시 저장
        if (timeCapsule.getIsTempSave()) {
            return TEMP_SAVED;
        }
        // 열림
        if (timeCapsule.getIsOpened()) {
            return OPENED;
        }

        LocalDateTime openAt = timeCapsule.getOpenedAt();
        LocalDateTime now = LocalDateTime.now();
        // 잠김
        if (now.isBefore(openAt)) {
            return LOCKED;
        }
        // 도착
        return ARRIVED;
    }
}
