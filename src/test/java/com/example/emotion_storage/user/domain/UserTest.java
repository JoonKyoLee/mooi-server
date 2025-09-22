package com.example.emotion_storage.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
}
