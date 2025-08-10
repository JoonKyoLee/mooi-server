package com.example.emotion_storage.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
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
        LocalDateTime birthday = LocalDateTime.of(1990, 1, 1, 0, 0);
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
                .nickName(nickName)
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
        assertThat(user.getNickName()).isEqualTo(nickName);
        assertThat(user.getGender()).isEqualTo(gender);
        assertThat(user.getBirthday()).isEqualTo(birthday);
        assertThat(user.getExpectations()).containsExactlyElementsOf(expectations);
        assertThat(user.isTermsAgreed()).isEqualTo(isTermsAgreed);
        assertThat(user.isPrivacyAgreed()).isEqualTo(isPrivacyAgreed);
        assertThat(user.isMarketingAgreed()).isEqualTo(isMarketingAgreed);
    }
}
