package com.example.emotion_storage.timecapsule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TimeCapsuleExistDateResponse(
        int totalDates,
        List<LocalDate> dates
) {}
