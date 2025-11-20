package com.example.emotion_storage.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.emotion_storage.attendance.response.AttendanceStreakStatusResponse;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AttendanceServiceTest {

    @Autowired private AttendanceService attendanceService;
    @Autowired private UserRepository userRepository;

    private User newUser(Integer streak, LocalDate rewardDate) {
        return userRepository.save(
                User.builder()
                        .socialType(SocialType.GOOGLE)
                        .socialId("social123")
                        .email("test@example.com")
                        .profileImageUrl("http://example.com/profile.png")
                        .nickname("MOOI")
                        .gender(Gender.MALE)
                        .birthday(LocalDate.of(2000, 1, 1))
                        .keyCount(5L)
                        .ticketCount(10L)
                        .isTermsAgreed(true)
                        .isPrivacyAgreed(true)
                        .isMarketingAgreed(false)
                        .appPushNotify(true)
                        .emotionReminderNotify(true)
                        .emotionReminderDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))
                        .emotionReminderTime(LocalTime.of(21, 0))
                        .timeCapsuleReportNotify(true)
                        .marketingInfoNotify(false)
                        .attendanceStreak(streak == null ? 0 : streak)
                        .lastAttendanceRewardDate(rewardDate)
                        .build()
        );
    }

    @Test
    void 출석_보상을_받은_기록이_없으면_보상을_받을_수_없다() {
        // given
        User user = newUser(0, null);

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.getAttendanceRewardStatus(user.getId());

        // then
        assertThat(response.streak()).isEqualTo(0);
        assertThat(response.isAttendedToday()).isFalse();
    }

    @Test
    void 오늘_이미_보상을_받았다면_보상_여부가_true로_반환된다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(3, today);

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.getAttendanceRewardStatus(user.getId());

        // then
        assertThat(response.streak()).isEqualTo(3);
        assertThat(response.isAttendedToday()).isTrue();
    }

    @Test
    void 어제_보상을_받았다면_오늘_보상을_받을_수_있다고_응답한다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(3, today.minusDays(1));

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.getAttendanceRewardStatus(user.getId());

        // then
        assertThat(response.streak()).isEqualTo(3);
        assertThat(response.isAttendedToday()).isFalse();
    }

    @Test
    void 일정기간_보상을_받지_않았다면_새로운_출석으로_판단한다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(4, today.minusDays(5));

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.getAttendanceRewardStatus(user.getId());

        // then
        assertThat(response.streak()).isEqualTo(0);
        assertThat(response.isAttendedToday()).isFalse();
    }

    @Test
    void 과거날짜로_요청하면_예외가_발생한다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(0, null);

        // when & then
        assertThatThrownBy(() ->
                attendanceService.updateAttendanceRewardStatus(user.getId(), today.minusDays(1))
        )
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_ATTENDANCE_REWARD.getMessage());
    }

    @Test
    void 첫_출석_보상은_streak가_1로_저장된다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(0, null);

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.updateAttendanceRewardStatus(user.getId(), today);

        // then
        assertThat(response.streak()).isEqualTo(1);
        assertThat(response.isAttendedToday()).isTrue();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getAttendanceStreak()).isEqualTo(1);
        assertThat(updated.getLastAttendanceRewardDate()).isEqualTo(today);
    }

    @Test
    void 오늘_이미_보상을_받았다면_예외가_발생한다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(2, today);

        // when & then
        assertThatThrownBy(() ->
                attendanceService.updateAttendanceRewardStatus(user.getId(), today)
        )
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.ALREADY_GET_ATTENDANCE_REWARD.getMessage());
    }

    @Test
    void 어제_보상을_받았다면_오늘_연속일수가_증가한다() {
        // given
        LocalDate today = LocalDate.now();
        int prev = 3;
        User user = newUser(prev, today.minusDays(1));

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.updateAttendanceRewardStatus(user.getId(), today);

        // then
        int expected = prev % 7 + 1;
        assertThat(response.streak()).isEqualTo(expected);
        assertThat(response.isAttendedToday()).isTrue();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getAttendanceStreak()).isEqualTo(expected);
        assertThat(updated.getLastAttendanceRewardDate()).isEqualTo(today);
    }

    @Test
    void 일정기간_보상을_받지_않은_상태에서_보상받으면_연속일수가_1로_설정된다() {
        // given
        LocalDate today = LocalDate.now();
        User user = newUser(5, today.minusDays(5));

        // when
        AttendanceStreakStatusResponse response =
                attendanceService.updateAttendanceRewardStatus(user.getId(), today);

        // then
        assertThat(response.streak()).isEqualTo(1);
        assertThat(response.isAttendedToday()).isTrue();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getAttendanceStreak()).isEqualTo(1);
        assertThat(updated.getLastAttendanceRewardDate()).isEqualTo(today);
    }
}
