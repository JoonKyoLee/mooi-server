package com.example.emotion_storage.timecapsule.service;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private final TimeCapsuleRepository timeCapsuleRepository;

    public ApiResponse<TimeCapsuleExistDateResponse> getMonthlyActiveDates(
            int year, int month, Long userId
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        log.info("{}년 {}월 타임캡슐 날짜 목록을 조회합니다.", year, month);
        List<LocalDate> dates =
                timeCapsuleRepository.findActiveDatesInRange(userId, start, end);
        int activeDays = dates.size();

        return ApiResponse.success(
                SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage(),
                new TimeCapsuleExistDateResponse(activeDays, dates)
        );
    }
}
