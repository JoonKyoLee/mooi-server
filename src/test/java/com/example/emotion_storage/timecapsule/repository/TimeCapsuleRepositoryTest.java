package com.example.emotion_storage.timecapsule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.emotion_storage.report.domain.Keyword;
import com.example.emotion_storage.report.domain.Report;
import com.example.emotion_storage.report.repository.ReportRepository;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class TimeCapsuleRepositoryTest {

    @Autowired TimeCapsuleRepository timeCapsuleRepository;
    @Autowired UserRepository userRepository;
    @Autowired ReportRepository reportRepository;

    private User user;
    private Report report;

    private final LocalDateTime BASE = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0);

    @BeforeEach
    void setup() {
        user = userRepository.save(User.builder()
                .socialType(SocialType.GOOGLE)
                .socialId("social123")
                .email("test@example.com")
                .nickname("tester")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2000,1,1))
                .keyCount(10L)
                .ticketCount(5L)
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isMarketingAgreed(false)
                .build());

        report = reportRepository.save(Report.builder()
                .historyDate(LocalDate.now())
                .todaySummary("요약")
                .stressIndex(30)
                .happinessIndex(80)
                .emotionSummary("감정")
                .isOpened(true)
                .build());

        report.addKeyword(Keyword.builder().keyword("집중").build());
        report.addKeyword(Keyword.builder().keyword("운동").build());
    }

    private void saveTimeCapsule(
            LocalDateTime historyDate, LocalDateTime openedAt, boolean isOpened, boolean isTempSave,
            boolean isFavorite, LocalDateTime favoriteAt, String title
    ) {
        timeCapsuleRepository.save(TimeCapsule.builder()
                .user(user)
                .report(report)
                .chatroomId(1L)
                .historyDate(historyDate)
                .openedAt(openedAt)
                .isOpened(isOpened)
                .isTempSave(isTempSave)
                .isFavorite(isFavorite)
                .favoriteAt(favoriteAt)
                .oneLineSummary(title)
                .dialogueSummary("대화")
                .myMindNote("노트")
                .build());
    }

    @Test
    void 특정_년월의_타임캡슐_존재_날짜를_중복없이_반환한다() {
        // given
        YearMonth yearMonth = YearMonth.of(BASE.getYear(), BASE.getMonthValue());
        LocalDateTime secondDateOfMonth = yearMonth.atDay(2).atTime(9, 0);
        LocalDateTime fifteenthDateOfMonth = yearMonth.atDay(15).atTime(15, 0);
        LocalDateTime someDateOfNextMonth = yearMonth.plusMonths(1).atDay(1).atTime(16, 0);

        saveTimeCapsule(secondDateOfMonth, secondDateOfMonth.plusDays(1), false, false, false, null, "1");
        saveTimeCapsule(secondDateOfMonth.plusHours(2), secondDateOfMonth.plusDays(2), false, false, false, null, "2");
        saveTimeCapsule(fifteenthDateOfMonth, fifteenthDateOfMonth.plusDays(5), false, false, false, null, "3");
        saveTimeCapsule(someDateOfNextMonth, someDateOfNextMonth.plusDays(3), false, false, false, null, "4");

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // when
        List<Date> dates = timeCapsuleRepository.findActiveDatesInRange(user.getId(), start, end);
        List<LocalDate> localDates = dates.stream()
                .map(Date::toLocalDate)
                .toList();

        // then
        assertThat(localDates.size()).isEqualTo(2);
        assertThat(localDates).containsExactly(
                yearMonth.atDay(2),
                yearMonth.atDay(15)
        );
    }

    @Test
    void 날짜에_따른_전체_타임캡슐_목록을_조회한다() {
        // given
        LocalDateTime targetStart = BASE.toLocalDate().atStartOfDay();
        LocalDateTime targetEnd = targetStart.plusDays(1);

        saveTimeCapsule(targetStart.plusHours(1), null, false, true, false, null, "1(임시 저장)");
        saveTimeCapsule(targetStart.plusHours(2), targetStart.plusHours(5), false, false, false, null, "2");
        saveTimeCapsule(targetStart.plusHours(3), targetStart.plusHours(7), false, false, false, null, "3");
        saveTimeCapsule(targetStart.minusHours(1), targetStart.plusDays(7), false, false, false, null, "전날");
        saveTimeCapsule(targetEnd.plusHours(1), targetStart.plusMonths(1), false, false, false, null, "다음날");

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("isTempSave").descending().and(Sort.by("historyDate").descending()));

        // when
        Page<TimeCapsule> timeCapsules = timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndHistoryDateBetween(
                user.getId(), targetStart, targetEnd, pageable
        );

        // then
        assertThat(timeCapsules.getTotalElements()).isEqualTo(3);

        assertThat(timeCapsules.getContent())
                .extracting(TimeCapsule::getOneLineSummary)
                .containsExactly(
                        "1(임시 저장)", "3", "2"
                );

        assertThat(timeCapsules.getContent())
                .allSatisfy(timeCapsule -> {
                    assertThat(timeCapsule.getHistoryDate()).isAfterOrEqualTo(targetStart);
                    assertThat(timeCapsule.getHistoryDate()).isBefore(targetEnd);
                });
    }

    @Test
    void 도착한_타임캡슐_목록을_반환한다() {
        // given
        LocalDateTime startDate = BASE.toLocalDate().minusDays(21).atStartOfDay();
        LocalDateTime endDate = BASE.toLocalDate().atStartOfDay();

        saveTimeCapsule(startDate.plusHours(1), startDate.plusDays(3), false, false, false, null, "1");
        saveTimeCapsule(startDate.plusHours(10), startDate.plusDays(5), false, false, false, null, "2");
        saveTimeCapsule(startDate.plusHours(20), startDate.plusDays(7), false, false, false, null, "3");
        saveTimeCapsule(endDate.minusHours(5), endDate.minusHours(2), true, false, false, null, "오픈1");
        saveTimeCapsule(endDate.minusDays(1), endDate.minusHours(1), true, false, false, null, "오픈2");

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("openedAt").descending());

        // when
        Page<TimeCapsule> timeCapsules = timeCapsuleRepository
                .findByUser_IdAndDeletedAtIsNullAndIsOpenedFalseAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
                        user.getId(), startDate, endDate, pageable
                );

        // then
        assertThat(timeCapsules.getTotalElements()).isEqualTo(3);

        assertThat(timeCapsules.getContent())
                .extracting(TimeCapsule::getOneLineSummary)
                .containsExactly("3", "2", "1");

        assertThat(timeCapsules.getContent())
                .allSatisfy(timeCapsule -> {
                    assertThat(timeCapsule.getIsOpened()).isFalse();
                    assertThat(timeCapsule.getOpenedAt()).isAfterOrEqualTo(startDate);
                    assertThat(timeCapsule.getOpenedAt()).isBeforeOrEqualTo(endDate);
                });
    }

    @Test
    void 즐겨찾기한_타임캡슐_목록을_최신_타임캡슐이_먼저_오도록_반환한다() {
        // given
        LocalDateTime startDate = BASE.toLocalDate().minusDays(21).atStartOfDay();
        LocalDateTime endDate = BASE.toLocalDate().atStartOfDay();

        saveTimeCapsule(startDate.plusHours(1), startDate.plusDays(3), false, false, false, null, "1");
        saveTimeCapsule(startDate.plusHours(10), startDate.plusDays(5), false, false, false, null, "2");
        saveTimeCapsule(startDate.plusHours(20), startDate.plusDays(7), false, false, false, null, "3");
        saveTimeCapsule(endDate.minusHours(5), endDate.minusHours(2), true, false, true, endDate.minusMinutes(10), "오픈1");
        saveTimeCapsule(endDate.minusHours(3), endDate.minusHours(1), true, false, true, endDate.minusMinutes(30), "오픈2");

        // when
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("historyDate").descending());
        Page<TimeCapsule> timeCapsules = timeCapsuleRepository
                .findByUser_IdAndDeletedAtIsNullAndIsFavoriteIsTrue(user.getId(), pageable);

        // then
        assertThat(timeCapsules.getTotalElements()).isEqualTo(2);

        assertThat(timeCapsules.getContent())
                .extracting(TimeCapsule::getOneLineSummary)
                .containsExactly("오픈2", "오픈1");

        assertThat(timeCapsules.getContent()).allSatisfy(timeCapsule -> assertThat(timeCapsule.getIsFavorite()).isTrue());
    }

    @Test
    void 즐겨찾기한_타임캡슐_목록을_즐겨찾기한_순서대로_반환한다() {
        // given
        LocalDateTime startDate = BASE.toLocalDate().minusDays(21).atStartOfDay();
        LocalDateTime endDate = BASE.toLocalDate().atStartOfDay();

        saveTimeCapsule(startDate.plusHours(1), startDate.plusDays(3), false, false, false, null, "1");
        saveTimeCapsule(startDate.plusHours(10), startDate.plusDays(5), false, false, false, null, "2");
        saveTimeCapsule(startDate.plusHours(20), startDate.plusDays(7), false, false, false, null, "3");
        saveTimeCapsule(endDate.minusHours(5), endDate.minusHours(2), true, false, true, endDate.minusMinutes(10), "오픈1");
        saveTimeCapsule(endDate.minusHours(3), endDate.minusHours(1), true, false, true, endDate.minusMinutes(30), "오픈2");

        // when
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("favoriteAt").descending());
        Page<TimeCapsule> timeCapsules = timeCapsuleRepository
                .findByUser_IdAndDeletedAtIsNullAndIsFavoriteIsTrue(user.getId(), pageable);

        // then
        assertThat(timeCapsules.getTotalElements()).isEqualTo(2);

        assertThat(timeCapsules.getContent())
                .extracting(TimeCapsule::getOneLineSummary)
                .containsExactly("오픈1", "오픈2");

        assertThat(timeCapsules.getContent()).allSatisfy(timeCapsule -> assertThat(timeCapsule.getIsFavorite()).isTrue());
    }

    @Test
    void 즐겨찾기한_타임캡슐의_개수를_반환한다() {
        // given
        LocalDateTime startDate = BASE.toLocalDate().minusDays(21).atStartOfDay();
        LocalDateTime endDate = BASE.toLocalDate().atStartOfDay();

        saveTimeCapsule(startDate.plusHours(1), startDate.plusDays(3), false, false, false, null, "1");
        saveTimeCapsule(startDate.plusHours(10), startDate.plusDays(5), false, false, false, null, "2");
        saveTimeCapsule(startDate.plusHours(20), startDate.plusDays(7), false, false, false, null, "3");
        saveTimeCapsule(endDate.minusHours(5), endDate.minusHours(2), true, false, true, endDate.minusMinutes(10), "오픈1");
        saveTimeCapsule(endDate.minusHours(3), endDate.minusHours(1), true, false, true, endDate.minusMinutes(30), "오픈2");

        // when
        int favoriteCount = timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(user.getId());

        // then
        assertThat(favoriteCount).isEqualTo(2);
    }
}
