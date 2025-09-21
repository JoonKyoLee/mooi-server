package com.example.emotion_storage.timecapsule.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.domain.TimeCapsuleOpenCost;
import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleFavoriteRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleNoteUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleDetailResponse;
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
    private static final String SORT_BY_TEMP_SAVED = "isTempSave";
    private static final String SORT_BY_DEFAULT_TIME = "historyDate";
    private static final String SORT_BY_ARRIVED_TIME = "openedAt";
    private static final String SORT_BY_FAVORITE_TIME = "favoriteAt";
    private static final int TIME_CAPSULE_FAVORITES_LIMIT = 30;
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    public TimeCapsuleExistDateResponse getMonthlyActiveDates(
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

        return new TimeCapsuleExistDateResponse(activeDays, dates);
    }

    public TimeCapsuleListResponse getTimeCapsuleList(
            LocalDate startDate, LocalDate endDate, int page, int limit, String status, Long userId
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        Pageable pageable;

        if (ARRIVED_STATUS.equals(status)) {
            log.info("사용자 {}의 {}-{}의 도착한 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            pageable = pageDesc(page, limit, SORT_BY_ARRIVED_TIME);
            LocalDateTime end = LocalDateTime.now();
            return getArrivedTimeCapsuleList(start, end, page, limit, userId, pageable);
        } else {
            log.info("사용자 {}의 {}-{}의 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            return getOneDayTimeCapsuleList(start, end, page, limit, userId);
        }
    }

    public TimeCapsuleListResponse getFavoriteTimeCapsules(
            int page, int limit, String sort, Long userId
    ) {
        final boolean sortFavorite = SORT_FAVORITE.equals(sort);
        Pageable pageable = pageDesc(page, limit, sortFavorite ? SORT_BY_FAVORITE_TIME : SORT_BY_DEFAULT_TIME);

        log.info("사용자 {}의 즐겨찾기 리스트를 조회합니다.", userId);
        return getFavoriteTimeCapsuleList(page, limit, userId, pageable);
    }

    @Transactional
    public TimeCapsuleFavoriteResponse setFavorite(
            Long timeCapsuleId, TimeCapsuleFavoriteRequest request, Long userId
    ) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        if (request.addFavorite()) {
            validateFavoriteLimit(userId);
            log.info("타임캡슐 {}을 즐겨찾기 목록에 추가합니다.", timeCapsule.getId());
            timeCapsule.markFavorite();

        } else {
            log.info("타임캡슐 {}을 즐겨찾기 목록에서 해제합니다.", timeCapsule.getId());
            timeCapsule.unmarkFavorite();
        }

        return new TimeCapsuleFavoriteResponse(
                timeCapsule.getIsFavorite(),
                timeCapsule.getFavoriteAt(),
                timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(userId)
        );
    }

    private Pageable pageDesc(int page, int limit, String sortField) {
        int zeroBasedPage = Math.max(0, page - 1);
        return PageRequest.of(zeroBasedPage, limit, Sort.by(sortField).descending());
    }

    private TimeCapsuleListResponse getOneDayTimeCapsuleList(
            LocalDateTime start, LocalDateTime end, int page, int limit, Long userId
    ) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(
                zeroBasedPage, limit,
                Sort.by(SORT_BY_TEMP_SAVED).descending().and(Sort.by(SORT_BY_DEFAULT_TIME).descending())
        );
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
                .map(TimeCapsuleDto::from)
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
        if (favoriteCnt >= TIME_CAPSULE_FAVORITES_LIMIT) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED);
        }
    }

    public TimeCapsuleDetailResponse getTimeCapsuleDetail(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);
        log.info("타임캡슐 {}에 대한 상세 정보를 조회합니다.", timeCapsuleId);
        return TimeCapsuleDetailResponse.from(timeCapsule);
    }

    @Transactional
    public void openTimeCapsule(Long timeCapsuleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        LocalDateTime openDate = timeCapsule.getOpenedAt();
        long days = calculateDaysToOpen(openDate);

        if (days != 0) {
            useKeysForOpening(user, days);
        }

        timeCapsule.setIsOpened(true);
    }

    private long calculateDaysToOpen(LocalDateTime openDate) {
        log.info("타임캡슐을 열 때까지 필요한 날을 계산합니다.");

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(openDate)) {
            return 0;
        }

        long seconds = Duration.between(now, openDate).getSeconds();
        return (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
    }

    private void useKeysForOpening(User user, long days) {
        log.info("타임캡슐을 열 때 필요한 열쇠의 개수를 계산합니다.");
        long requiredKeys = TimeCapsuleOpenCost.getRequiredKeys(days);
        user.consumeKeys(requiredKeys);
        log.info("열쇠 {}개를 사용했습니다. 남은 열쇠의 개수는 {}개입니다.", requiredKeys, user.getKeyCount());
    }

    @Transactional
    public void updateTimeCapsuleNote(Long timeCapsuleId, TimeCapsuleNoteUpdateRequest request, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        log.info("타임캡슐 {}의 내 마음 노트를 업데이트 합니다.", timeCapsuleId);
        timeCapsule.updateMyMindNote(request.content());
    }

    @Transactional
    public void deleteTimeCapsule(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        log.info("타임캡슐 {}를 삭제합니다.", timeCapsuleId);
        timeCapsule.setDeletedAt(LocalDateTime.now());
    }
}
