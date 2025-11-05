package com.example.emotion_storage.mypage.dto.response;

import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import java.time.LocalDate;

public record UserAccountInfoResponse(
        String nickname,
        String email,
        SocialType socialType,
        Gender gender,
        LocalDate birthday,
        long days
) {}
