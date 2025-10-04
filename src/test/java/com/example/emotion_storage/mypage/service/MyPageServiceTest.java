package com.example.emotion_storage.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MyPageServiceTest {

    @Autowired MyPageService myPageService;
    @Autowired UserRepository userRepository;
    @Autowired TokenService tokenService;

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
}
