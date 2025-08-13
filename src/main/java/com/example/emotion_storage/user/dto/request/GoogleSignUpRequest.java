package com.example.emotion_storage.user.dto.request;

import com.example.emotion_storage.user.domain.Gender;
import java.time.LocalDate;
import java.util.List;

public record GoogleSignUpRequest(
        String nickname,
        Gender gender,
        LocalDate birthday,
        List<String> expectations,
        boolean isTermsAgreed,
        boolean isPrivacyAgreed,
        boolean isMarketingAgreed,
        String idToken
) {}
