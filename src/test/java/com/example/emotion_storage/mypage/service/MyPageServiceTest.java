package com.example.emotion_storage.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.request.NotificationSettingsUpdateRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.mypage.dto.response.UserKeyCountResponse;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MyPageServiceTest {

    @Autowired MyPageService myPageService;
    @Autowired UserRepository userRepository;
    @MockitoBean TokenService tokenService;

    private Long userId;

    @BeforeEach
    void setup() {
        // 유저 생성
        User user = User.builder()
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
                .build();

        userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void 마이페이지_초기_화면_정보를_반환한다() {
        // when
        MyPageOverviewResponse response = myPageService.getMyPageOverview(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("MOOI");
        assertThat(response.days()).isEqualTo(1L);
        assertThat(response.keys()).isEqualTo(5L);
    }

    @Test
    void 닉네임_변경에_성공한다() {
        // given
        User user = userRepository.findById(userId).orElseThrow();
        String nicknameToChange = "모이";
        NicknameChangeRequest request = new NicknameChangeRequest(nicknameToChange);
        String beforeNickname = user.getNickname();

        // when
        myPageService.changeUserNickname(request, userId);

        // then
        User changed = userRepository.findById(userId).orElseThrow();
        String afterNickname = changed.getNickname();
        assertThat(beforeNickname).isNotEqualTo(afterNickname);
        assertThat(afterNickname).isEqualTo("모이");
    }

    @Test
    void 사용자_열쇠_개수_조회에_성공한다() {
        // when
        UserKeyCountResponse response = myPageService.getUserKeyCount(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.keyCount()).isEqualTo(5L);
    }

    @Test
    void 사용자_계정_정보_조회에_성공한다() {
        // when
        UserAccountInfoResponse response = myPageService.getUserAccountInfo(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.socialType()).isEqualTo(SocialType.GOOGLE);
        assertThat(response.gender()).isEqualTo(Gender.MALE);
        assertThat(response.birthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void 사용자_알림_설정_상태_정보_조회에_성공한다() {
        // when
        NotificationSettingsResponse response = myPageService.getNotificationSettings(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.appPushNotify()).isTrue();
        assertThat(response.emotionReminderNotify()).isTrue();
        assertThat(response.emotionReminderDays()).isEqualTo(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        assertThat(response.emotionReminderTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(response.timeCapsuleReportNotify()).isTrue();
        assertThat(response.marketingInfoNotify()).isFalse();
    }

    @Test
    void 사용자_알림_설정_상태_업데이트에_성공한다() {
        // given
        NotificationSettingsUpdateRequest request = new NotificationSettingsUpdateRequest(
                true, false, null, null,
                true, true
        );

        // when
        myPageService.updateNotificationSettings(request, userId);

        // then
        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.isAppPushNotify()).isTrue();
        assertThat(user.isEmotionReminderNotify()).isFalse();
        assertThat(user.getEmotionReminderDays()).isNull();
        assertThat(user.getEmotionReminderTime()).isNull();
        assertThat(user.isTimeCapsuleReportNotify()).isTrue();
        assertThat(user.isMarketingInfoNotify()).isTrue();
    }

    @Test
    void 탈퇴_처리가_잘_진행된다() {
        // given
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        // when
        myPageService.withdrawUser(userId, request, response);

        // then
        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getDeletedAt()).isNotNull();
        verify(tokenService).revokeTokens(request, response, userId);
    }

    @Test
    void 로그아웃_처리가_잘_진행된다() {
        // given
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        // when
        myPageService.logout(userId, request, response);

        // then
        verify(tokenService).revokeTokens(request, response, userId);
    }
}
