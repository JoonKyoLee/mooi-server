package com.example.emotion_storage.user.dto.request;

import com.example.emotion_storage.user.domain.Gender;
import java.time.LocalDateTime;
import java.util.List;

public record GoogleSignUpRequest(
        String nickname,
        Gender gender,
        LocalDateTime birthday,
        List<String> expectations,
        boolean isTermsAgreed,
        boolean isPrivacyAgreed,
        boolean isMarketingAgreed,
        String idToken
) {}
