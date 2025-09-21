package com.example.emotion_storage.timecapsule.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.emotion_storage.report.domain.Report;
import com.example.emotion_storage.report.repository.ReportRepository;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TimeCapsuleServiceTest {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @Autowired
    private TimeCapsuleRepository timeCapsuleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    private Long userId;

    @BeforeEach
    void setup() {
        // 1. 유저 생성
        User user = User.builder()
                .socialType(SocialType.GOOGLE)
                .socialId("social123")
                .email("test@example.com")
                .profileImageUrl("http://example.com/profile.png")
                .nickname("tester")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2000,1,1))
                .keyCount(5L)
                .ticketCount(10L)
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isMarketingAgreed(false)
                .build();

        userRepository.save(user);
        userId = user.getId();

        // 2. 리포트 생성
        Report report = Report.builder()
                .historyDate(LocalDate.now())
                .todaySummary("오늘 하루 요약")
                .stressIndex(3)
                .happinessIndex(7)
                .emotionSummary("감정 요약")
                .keywords("공부, 프로젝트")
                .isOpened(true)
                .build();

        reportRepository.save(report);

        // 3. 타임캡슐 생성
        for (int i = 1; i <= 20; i++) {
            boolean isFavorite = i % 2 == 0;
            int group = (i + 1) / 2;
            boolean isOne = i == 1;

            LocalDateTime historyDate = LocalDateTime.now().minusDays(group);
            LocalDateTime favoriteAt = LocalDateTime.now().minusHours(i);
            LocalDateTime openAt = LocalDateTime.now().minusHours(i);

            TimeCapsule timeCapsule = TimeCapsule.builder()
                    .user(user)
                    .report(report)
                    .chatroomId(100L + i)
                    .historyDate(historyDate)
                    .favoriteAt(favoriteAt)
                    .openedAt(isOne ? null : openAt)
                    .oneLineSummary("한 줄 요약 " + i)
                    .dialogueSummary("대화 요약 " + i)
                    .myMindNote("내 마음 노트 " + i)
                    .isOpened(false)
                    .isTempSave(isOne)
                    .isFavorite(isFavorite)
                    .build();

            timeCapsuleRepository.save(timeCapsule);
        }
    }

    @Test
    void 타임캡슐이_존재하는_날짜_목록을_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int totalDays = Math.min(10, Math.max(0, day - 1));

        // when
        TimeCapsuleExistDateResponse response =
                timeCapsuleService.getActiveDatesForMonth(year, month, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalDates()).isEqualTo(totalDays);
        assertThat(response.dates()).hasSize(totalDays);

        assertThat(response.dates()).allSatisfy(dates -> {
            assertThat(dates.getYear()).isEqualTo(year);
            assertThat(dates.getMonthValue()).isEqualTo(month);
        });
    }

    @Test
    void 특정_날짜의_타임캡슐_목록을_조회한다() {
        // given
        LocalDate targetDate = LocalDate.now().minusDays(1);
        int page = 1;
        int limit = 10;

        // when
        TimeCapsuleListResponse response =
                timeCapsuleService.fetchTimeCapsules(targetDate, targetDate, page, limit, "all", userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCapsules()).isEqualTo(2);
        assertThat(response.pagination().page()).isEqualTo(page);
        assertThat(response.pagination().limit()).isEqualTo(limit);
        assertThat(response.pagination().totalPage()).isEqualTo(1);

        // 정렬 검증: 임시저장(true)인 항목이 먼저
        assertThat(response.timeCapsules()).hasSize(2);
        assertThat(response.timeCapsules().get(0).title()).isEqualTo("한 줄 요약 1");
        assertThat(response.timeCapsules().get(1).title()).isEqualTo("한 줄 요약 2");
    }

    @Test
    void 도착한_날짜의_타임캡슐을_조회한다() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(21);
        LocalDate endDate = LocalDate.now();
        int page = 1;
        int limit = 10;

        // when
        TimeCapsuleListResponse response =
                timeCapsuleService.fetchTimeCapsules(startDate, endDate, page, limit, "arrived", userId);

        // then
        assertThat(response).isNotNull();

        assertThat(response.pagination().page()).isEqualTo(page);
        assertThat(response.pagination().limit()).isEqualTo(limit);
        assertThat(response.pagination().totalPage()).isEqualTo(2);

        assertThat(response.totalCapsules()).isEqualTo(limit);
        assertThat(response.timeCapsules()).hasSize(limit);

        // 도착한 타임캡슐의 경우에는 임시저장 항목이 들어가지 않기 때문에 2부터 확인(도착 시간이 null이기 때문)
        assertThat(response.timeCapsules().get(0).title()).isEqualTo("한 줄 요약 2");
        assertThat(response.timeCapsules().get(9).title()).isEqualTo("한 줄 요약 11");

        for (int i = 0; i < response.timeCapsules().size() - 1; i++) {
            assertThat(response.timeCapsules().get(i).openAt())
                    .isAfterOrEqualTo(response.timeCapsules().get(i + 1).openAt());
        }
    }

    @Test
    void 즐겨찾기된_타임캡슐을_타임캡슐_날짜_순으로_조회한다() {
        // given
        int page = 1;
        int limit = 10;
        String sort = "favorite";

        // when
        TimeCapsuleListResponse response = timeCapsuleService.fetchFavoriteTimeCapsules(page, limit, sort, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pagination().page()).isEqualTo(page);
        assertThat(response.pagination().limit()).isEqualTo(limit);

        assertThat(response.totalCapsules()).isEqualTo(limit);
        assertThat(response.timeCapsules()).hasSize(limit);

        assertThat(response.timeCapsules().get(0).title()).isEqualTo("한 줄 요약 2");
        assertThat(response.timeCapsules().get(9).title()).isEqualTo("한 줄 요약 20");

        for (int i = 0; i < response.timeCapsules().size() - 1; i++) {
            assertThat(response.timeCapsules().get(i).historyDate())
                    .isAfterOrEqualTo(response.timeCapsules().get(i + 1).historyDate());
        }
    }

    @Test
    void 즐겨찾기된_타임캡슐을_즐겨찾기한_순으로_조회한다() {
        // given
        int page = 1;
        int limit = 10;
        String sort = "all";

        // when
        TimeCapsuleListResponse response = timeCapsuleService.fetchFavoriteTimeCapsules(page, limit, sort, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pagination().page()).isEqualTo(page);
        assertThat(response.pagination().limit()).isEqualTo(limit);

        assertThat(response.totalCapsules()).isEqualTo(limit);
        assertThat(response.timeCapsules()).hasSize(limit);

        assertThat(response.timeCapsules().get(0).title()).isEqualTo("한 줄 요약 2");
        assertThat(response.timeCapsules().get(9).title()).isEqualTo("한 줄 요약 20");
    }
}
