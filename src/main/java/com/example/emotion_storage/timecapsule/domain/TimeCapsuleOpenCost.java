package com.example.emotion_storage.timecapsule.domain;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeCapsuleOpenCost {
    ONE_KEY(1, 7, 1),
    THREE_KEYS(8, 30, 3),
    SEVEN_KEYS(31, 90, 7),
    ELEVEN_KEYS(91, 180, 11),
    FIFTEEN_KEYS(181, Integer.MAX_VALUE, 15);

    private final int minDays;
    private final int maxDays;
    private final int requiredKeys;

    public static int getRequiredKeys(long days) {
        return Arrays.stream(TimeCapsuleOpenCost.values())
                .filter(policy -> policy.minDays <= days && days <= policy.maxDays)
                .findFirst()
                .map(TimeCapsuleOpenCost::getRequiredKeys)
                .orElseThrow(() -> new BaseException(ErrorCode.TIME_CAPSULE_OPEN_RULE_NOT_FOUND));
    }
}
