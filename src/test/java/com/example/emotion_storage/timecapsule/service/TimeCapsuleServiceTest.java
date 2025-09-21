package com.example.emotion_storage.timecapsule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.repository.UserRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class TimeCapsuleServiceTest {

    private static final String ALL_STATUS = "all";
    private static final String ARRIVED_STATUS = "arrived";
    private static final String SORT_FAVORITE = "favorite";
    private static final String SORT_BY_TEMP_SAVED = "isTempSave";
    private static final String SORT_BY_DEFAULT_TIME = "historyDate";
    private static final String SORT_BY_ARRIVED_TIME = "openedAt";
    private static final String SORT_BY_FAVORITE_TIME = "favoriteAt";

    @InjectMocks private TimeCapsuleService timeCapsuleService;
    @Mock private UserRepository userRepository;
    @Mock private TimeCapsuleRepository timeCapsuleRepository;

    private TimeCapsule sampleCapsule(Long id, boolean isTempSave, boolean isOpened, LocalDateTime openAt, boolean isFavorite) {
        // 임의의 최소 필드만 세팅(연관관계/필수 컬럼은 프로젝트 엔티티 제약에 맞게 보완해도 OK)
        return TimeCapsule.builder()
                .id(id)
                .user(null)
                .report(null)
                .chatroomId(1000L + id)
                .historyDate(LocalDateTime.now().minusDays(5))
                .oneLineSummary("요약 " + id)
                .dialogueSummary("대화요약 " + id)
                .myMindNote("노트 " + id)
                .favoriteAt(isFavorite ? LocalDateTime.now().minusHours(1) : null)
                .openedAt(openAt)
                .isOpened(isOpened)
                .isTempSave(isTempSave)
                .isFavorite(isFavorite)
                .build();
    }

    @Test
    void 타임캡슐이_존재하는_날짜_목록을_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        Long userId = 1L;

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Date> repoResult = List.of(
                Date.valueOf(LocalDate.of(year, month, 2)),
                Date.valueOf(LocalDate.of(year, month, 18))
        );

        when(timeCapsuleRepository.findActiveDatesInRange(eq(userId), eq(start), eq(end)))
                .thenReturn(repoResult);

        // when
        ApiResponse<TimeCapsuleExistDateResponse> response = timeCapsuleService.getMonthlyActiveDates(year, month, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message())
                .isEqualTo(SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage());

        TimeCapsuleExistDateResponse data = response.data();
        assertThat(data.totalDates()).isEqualTo(2);
        assertThat(data.dates()).containsExactly(
                LocalDate.of(year, month, 2),
                LocalDate.of(year, month, 18)
        );
    }

    @Test
    void 특정_날짜의_타임캡슐_목록을_조회한다() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int page = 1;
        int limit = 10;
        Long userId = 1L;

        TimeCapsule a = sampleCapsule(201L, false, false, LocalDateTime.now().minusSeconds(5), false);
        TimeCapsule b = sampleCapsule(202L, true, true,  LocalDateTime.now().minusSeconds(30), true);

        Page<TimeCapsule> capsules = new PageImpl<>(List.of(b, a), PageRequest.of(0, limit), 2);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndHistoryDateBetween(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), pageableCaptor.capture()
        )).thenReturn(capsules);

        // when
        ApiResponse<TimeCapsuleListResponse> response =
                timeCapsuleService.getTimeCapsuleList(startDate, endDate, page, limit, ALL_STATUS, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage());

        TimeCapsuleListResponse data = response.data();
        assertThat(data.totalCapsules()).isEqualTo(2);
        assertThat(data.pagination().page()).isEqualTo(page);
        assertThat(data.pagination().limit()).isEqualTo(limit);
        assertThat(data.pagination().totalPage()).isEqualTo(1);

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(limit);

        Sort sort = used.getSort();
        List<Sort.Order> orders = StreamSupport.stream(sort.spliterator(), false).toList();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo(SORT_BY_TEMP_SAVED);
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1).getProperty()).isEqualTo(SORT_BY_DEFAULT_TIME);
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void 도착한_타임캡슐_목록을_조회한다() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        int page = 1;
        int limit = 10;
        Long userId = 1L;

        TimeCapsule a = sampleCapsule(201L, false, false, LocalDateTime.now().minusSeconds(5), false);
        TimeCapsule b = sampleCapsule(202L, false, true,  LocalDateTime.now().minusSeconds(30), true);

        Page<TimeCapsule> capsules = new PageImpl<>(List.of(a, b), PageRequest.of(0, limit), 2);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndIsOpenedFalseAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), pageableCaptor.capture()
        )).thenReturn(capsules);

        // when
        ApiResponse<TimeCapsuleListResponse> response =
                timeCapsuleService.getTimeCapsuleList(startDate, endDate, page, limit, ARRIVED_STATUS, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo(SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage());

        TimeCapsuleListResponse data = response.data();
        assertThat(data.totalCapsules()).isEqualTo(2);
        assertThat(data.pagination().page()).isEqualTo(page);
        assertThat(data.pagination().limit()).isEqualTo(limit);
        assertThat(data.pagination().totalPage()).isEqualTo(1);

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(limit);
        Sort.Order order = used.getSort().getOrderFor(SORT_BY_ARRIVED_TIME);
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
