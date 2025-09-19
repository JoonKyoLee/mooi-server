package com.example.emotion_storage.timecapsule.service;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.domain.TimeCapsuleOpenCost;
import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleFavoriteRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleNoteUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleFavoriteResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.sql.Date;
import java.time.Duration;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final String ARRIVED_STATUS = "arrived";
    private static final String SORT_FAVORITE = "favorite";
    private static final String SORT_BY_DEFAULT_TIME = "historyDate";
    private static final String SORT_BY_ARRIVED_TIME = "openedAt";
    private static final String SORT_BY_FAVORITE_TIME = "favoriteAt";

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

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
            pageable = pageDesc(page, limit, SORT_BY_ARRIVED_TIME);
            LocalDateTime end = LocalDateTime.now();
            timeCapsuleList = getArrivedTimeCapsuleList(start, end, page, limit, userId, pageable);
        } else {
            log.info("사용자 {}의 {}-{}의 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            pageable = pageDesc(page, limit, SORT_BY_DEFAULT_TIME);
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            timeCapsuleList = getOneDayTimeCapsuleList(start, end, page, limit, userId, pageable);
        }

        return ApiResponse.success(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage(), timeCapsuleList);
    }

    public ApiResponse<TimeCapsuleListResponse> getFavoriteTimeCapsules(
            int page, int limit, String sort, Long userId
    ) {
        final boolean sortFavorite = SORT_FAVORITE.equals(sort);
        Pageable pageable = pageDesc(page, limit, sortFavorite ? SORT_BY_FAVORITE_TIME : SORT_BY_DEFAULT_TIME);

        log.info("사용자 {}의 즐겨찾기 리스트를 조회합니다.", userId);
        TimeCapsuleListResponse timeCapsuleList = getFavoriteTimeCapsuleList(page, limit, userId, pageable);

        return ApiResponse.success(SuccessMessage.GET_FAVORITE_TIME_CAPSULE_LIST_SUCCESS.getMessage(), timeCapsuleList);
    }

    @Transactional
    public ApiResponse<TimeCapsuleFavoriteResponse> setFavorite(
            Long timeCapsuleId, TimeCapsuleFavoriteRequest request, Long userId
    ) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        if (request.addFavorite()) {
            validateFavoriteLimit(userId);
            addFavorite(timeCapsule);

        } else {
            deleteFavorite(timeCapsule);
        }

        TimeCapsuleFavoriteResponse response = new TimeCapsuleFavoriteResponse(
                timeCapsule.getIsFavorite(),
                timeCapsule.getFavoriteAt(),
                timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(userId)
        );

        SuccessMessage successMessage = request.addFavorite()
                ? SuccessMessage.ADD_FAVORITE_TIME_CAPSULE_SUCCESS
                : SuccessMessage.REMOVE_FAVORITE_TIME_CAPSULE_SUCCESS;

        return ApiResponse.success(successMessage.getMessage(), response);
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
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndIsOpenedFalseAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
                        userId, start, end, pageable
                );
        return getTimeCapsuleList(timeCapsules, page, limit);
    }

    private TimeCapsuleListResponse getFavoriteTimeCapsuleList(
            int page, int limit, Long userId, Pageable pageable
    ) {
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndIsFavoriteIsTrue(userId, pageable);
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

    private TimeCapsule findOwnedTimeCapsule(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = timeCapsuleRepository.findById(timeCapsuleId)
                .orElseThrow(() -> new BaseException(ErrorCode.TIME_CAPSULE_NOT_FOUND));

        if (!timeCapsule.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_IS_NOT_OWNED);
        }
        return timeCapsule;
    }

    private void validateFavoriteLimit(Long userId) {
        log.info("즐겨찾기된 타임캡슐 개수를 조회합니다.");
        int favoriteCnt = timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(userId);
        if (favoriteCnt >= 30) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED);
        }
    }

    private void addFavorite(TimeCapsule timeCapsule) {
        log.info("타임캡슐 {}을 즐겨찾기 목록에 추가합니다.", timeCapsule.getId());
        timeCapsule.setFavoriteAt(LocalDateTime.now());
        timeCapsule.setIsFavorite(true);
    }

    private void deleteFavorite(TimeCapsule timeCapsule) {
        log.info("타임캡슐 {}을 즐겨찾기 목록에서 해제합니다.", timeCapsule.getId());
        timeCapsule.setFavoriteAt(null);
        timeCapsule.setIsFavorite(false);
    }

    @Transactional
    public ApiResponse<Void> openTimeCapsule(Long timeCapsuleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        LocalDateTime openDate = timeCapsule.getOpenedAt();
        long days = calculateDaysToOpen(openDate);

        if (days != 0) {
            useKeysForOpening(user, days);
        }

        timeCapsule.setIsOpened(true);

        return ApiResponse.success(204, SuccessMessage.OPEN_TIME_CAPSULE_SUCCESS.getMessage(), null);
    }

    private long calculateDaysToOpen(LocalDateTime openDate) {
        log.info("타임캡슐을 열 때까지 필요한 날을 계산합니다.");

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(openDate)) {
            return 0;
        }

        long seconds = Duration.between(now, openDate).getSeconds();
        long secondsPerDay = 24 * 60 * 60;

        return (seconds + secondsPerDay - 1) / secondsPerDay;
    }

    private void useKeysForOpening(User user, long days) {
        log.info("타임캡슐을 열 때 필요한 열쇠의 개수를 계산합니다.");

        long keys = TimeCapsuleOpenCost.getRequiredKeys(days);
        long currentKeys = user.getKeyCount();
        if (currentKeys < keys) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_KEY_NOT_ENOUGH);
        }

        user.updateKeyCount(keys);
        log.info("열쇠 {}개를 사용했습니다. 남은 열쇠의 개수는 {}개입니다.", keys, user.getKeyCount());
    }

    @Transactional
    public ApiResponse<Void> updateTimeCapsuleNote(Long timeCapsuleId, TimeCapsuleNoteUpdateRequest request, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        log.info("타임캡슐 {}의 내 마음 노트를 업데이트 합니다.", timeCapsuleId);
        timeCapsule.updateMyMindNote(request.content());

        return ApiResponse.success(204, SuccessMessage.UPDATE_TIME_CAPSULE_MIND_NOTE_SUCCESS.getMessage(), null);
    }
}
