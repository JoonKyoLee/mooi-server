package com.example.emotion_storage.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    void 정상적으로_유저가_생성된다() {
        // given
        SocialType socialType = SocialType.GOOGLE;
        String socialId = "social-id";
        String email = "test@email.com";
        String profileImgUrl = "https://image-url.com/image.jpg";
        String nickName = "모이";
        Gender gender = Gender.FEMALE;
        LocalDate birthday = LocalDate.of(1990, 1, 1);
        List<String> expectations = List.of("내 감정을 정리하고 싶어요", "내 감정 패턴을 알고 싶어요");
        boolean isTermsAgreed = true;
        boolean isPrivacyAgreed = true;
        boolean isMarketingAgreed = false;
        Long keyCount = 5L;
        Long ticketCount = 10L;
        boolean appPushNotify = true;
        boolean emotionReminderNotify = true;
        Set<DayOfWeek> emotionReminderDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        LocalTime emotionReminderTime = LocalTime.of(21, 0);
        boolean timeCapsuleReportNotify = true;
        boolean marketingInfoNotify = false;

        // when
        User user = User.builder()
                .socialType(socialType)
                .socialId(socialId)
                .email(email)
                .profileImageUrl(profileImgUrl)
                .nickname(nickName)
                .gender(gender)
                .birthday(birthday)
                .expectations(expectations)
                .isTermsAgreed(isTermsAgreed)
                .isPrivacyAgreed(isPrivacyAgreed)
                .isMarketingAgreed(isMarketingAgreed)
                .keyCount(keyCount)
                .ticketCount(ticketCount)
                .appPushNotify(appPushNotify)
                .emotionReminderNotify(emotionReminderNotify)
                .emotionReminderDays(emotionReminderDays)
                .emotionReminderTime(emotionReminderTime)
                .timeCapsuleReportNotify(timeCapsuleReportNotify)
                .marketingInfoNotify(marketingInfoNotify)
                .build();

        // then
        assertThat(user.getSocialType()).isEqualTo(socialType);
        assertThat(user.getSocialId()).isEqualTo(socialId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImgUrl);
        assertThat(user.getNickname()).isEqualTo(nickName);
        assertThat(user.getGender()).isEqualTo(gender);
        assertThat(user.getBirthday()).isEqualTo(birthday);
        assertThat(user.getExpectations()).containsExactlyElementsOf(expectations);
        assertThat(user.isTermsAgreed()).isEqualTo(isTermsAgreed);
        assertThat(user.isPrivacyAgreed()).isEqualTo(isPrivacyAgreed);
        assertThat(user.isMarketingAgreed()).isEqualTo(isMarketingAgreed);
        assertThat(user.getKeyCount()).isEqualTo(keyCount);
        assertThat(user.getTicketCount()).isEqualTo(ticketCount);
        assertThat(user.isAppPushNotify()).isEqualTo(appPushNotify);
        assertThat(user.isEmotionReminderNotify()).isEqualTo(emotionReminderNotify);
        assertThat(user.getEmotionReminderDays()).isEqualTo(emotionReminderDays);
        assertThat(user.getEmotionReminderTime()).isEqualTo(emotionReminderTime);
        assertThat(user.isTimeCapsuleReportNotify()).isEqualTo(timeCapsuleReportNotify);
        assertThat(user.isMarketingInfoNotify()).isEqualTo(marketingInfoNotify);
    }

    @Test
    void 열쇠_차감에_성공한다() {
        // given
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("socialId")
                .email("test@example.com")
                .nickname("mooi")
                .gender(Gender.MALE)
                .keyCount(10L)
                .build();

        Long requiredKeys = 4L;

        // when
        user.consumeKeys(requiredKeys);

        // then
        assertThat(user.getKeyCount()).isEqualTo(6L);
    }

    @Test
    void 알림_상태_정보_업데이트에_성공한다() {
        // given
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("socialId")
                .email("test@example.com")
                .nickname("mooi")
                .gender(Gender.MALE)
                .keyCount(10L)
                .appPushNotify(true)
                .emotionReminderNotify(true)
                .emotionReminderDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))
                .emotionReminderTime(LocalTime.of(21, 0))
                .timeCapsuleReportNotify(true)
                .marketingInfoNotify(true)
                .build();

        boolean appPushNotify = false;
        boolean emotionReminderNotify = false;
        Set<DayOfWeek> emotionReminderDays = null;
        LocalTime emotionReminderTime = null;
        boolean timeCapsuleReportNotify = false;
        boolean marketingInfoNotify = true;

        // when
        user.updateAppPushNotify(appPushNotify);
        user.updateEmotionReminder(emotionReminderNotify, emotionReminderDays, emotionReminderTime);
        user.updateTimeCapsuleReportNotify(timeCapsuleReportNotify);
        user.updateMarketingInfoNotify(marketingInfoNotify);

        // then
        assertThat(user.isAppPushNotify()).isEqualTo(appPushNotify);
        assertThat(user.isEmotionReminderNotify()).isEqualTo(emotionReminderNotify);
        assertThat(user.getEmotionReminderDays()).isEqualTo(emotionReminderDays);
        assertThat(user.getEmotionReminderTime()).isEqualTo(emotionReminderTime);
        assertThat(user.isTimeCapsuleReportNotify()).isEqualTo(timeCapsuleReportNotify);
        assertThat(user.isMarketingInfoNotify()).isEqualTo(marketingInfoNotify);
    }

    @Test
    void 소셜_타입_판별에_성공한다() {
        // given
        User googleUser = User.builder()
                .socialType(SocialType.GOOGLE)
                .build();

        User kakaoUser = User.builder()
                .socialType(SocialType.KAKAO)
                .build();

        // when, then
        assertThat(googleUser.isGoogleType()).isTrue();
        assertThat(googleUser.isKakaoType()).isFalse();
        assertThat(kakaoUser.isKakaoType()).isTrue();
        assertThat(kakaoUser.isGoogleType()).isFalse();
    }
}
