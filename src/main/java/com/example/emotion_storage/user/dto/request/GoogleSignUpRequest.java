package com.example.emotion_storage.user.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record GoogleSignUpRequest(
        String nickname,
        String gender,
        LocalDateTime birthday,
        List<String> expectations,
        boolean isTermsAgreed,
        boolean isPrivacyAgreed,
        boolean isMarketingAgreed,
        String idToken
) {}
