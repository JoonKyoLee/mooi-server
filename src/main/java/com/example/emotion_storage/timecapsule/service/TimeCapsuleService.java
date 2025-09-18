package com.example.emotion_storage.timecapsule.service;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final String ARRIVED_STATUS = "arrived";
    private static final String SORT_FOR_ARRIVED = "openedAt";
    private static final String SORT_FOR_ALL = "historyDate";

    private final TimeCapsuleRepository timeCapsuleRepository;

    public ApiResponse<TimeCapsuleExistDateResponse> getMonthlyActiveDates(
            int year, int month, Long userId
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        log.info("{}년 {}월에 타임캡슐이 존재하는 날짜 목록을 조회합니다.", year, month);
        List<LocalDate> dates =
                timeCapsuleRepository.findActiveDatesInRange(userId, start, end).stream()
                        .map(Date::toLocalDate)
                        .distinct()
                        .toList();

        int activeDays = dates.size();

        return ApiResponse.success(
                SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage(),
                new TimeCapsuleExistDateResponse(activeDays, dates)
        );
    }

    public ApiResponse<TimeCapsuleListResponse> getTimeCapsuleList(
            LocalDate startDate, LocalDate endDate, int page, int limit, String status, Long userId
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        TimeCapsuleListResponse timeCapsuleList;
        Pageable pageable;

        if (ARRIVED_STATUS.equals(status)) {
            log.info("사용자 {}의 {}-{}의 도착한 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            pageable = pageDesc(page, limit, SORT_FOR_ARRIVED);
            LocalDateTime end = LocalDateTime.now();
            timeCapsuleList = getArrivedTimeCapsuleList(start, end, page, limit, userId, pageable);
        } else {
            log.info("사용자 {}의 {}-{}의 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            pageable = pageDesc(page, limit, SORT_FOR_ALL);
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            timeCapsuleList = getOneDayTimeCapsuleList(start, end, page, limit, userId, pageable);
        }

        return ApiResponse.success(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage(), timeCapsuleList);
    }

    private Pageable pageDesc(int page, int limit, String sortField) {
        return PageRequest.of(page, limit, Sort.by(sortField).descending());
    }

    private TimeCapsuleListResponse getOneDayTimeCapsuleList(
            LocalDateTime start, LocalDateTime end, int page, int limit, Long userId, Pageable pageable
    ) {
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndHistoryDateBetween(
                        userId, start, end, pageable
                );
        return getTimeCapsuleList(timeCapsules, page, limit);
    }

    private TimeCapsuleListResponse getArrivedTimeCapsuleList(
            LocalDateTime start, LocalDateTime end, int page, int limit, Long userId, Pageable pageable
    ) {
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
                        userId, start, end, pageable
                );
        return getTimeCapsuleList(timeCapsules, page, limit);
    }

    private TimeCapsuleListResponse getTimeCapsuleList(Page<TimeCapsule> timeCapsules, int page, int limit) {
        List<TimeCapsuleDto> timeCapsuleList = makeTimeCapsuleListFormat(timeCapsules);

        return new TimeCapsuleListResponse(
                new PaginationDto(
                        page, limit, timeCapsules.getTotalPages()
                ),
                timeCapsules.getNumberOfElements(),
                timeCapsuleList
        );
    }

    private List<TimeCapsuleDto> makeTimeCapsuleListFormat(Page<TimeCapsule> timeCapsules) {
        return timeCapsules.getContent().stream()
                .map(TimeCapsuleDto::of)
                .toList();
    }
}
