package com.example.emotion_storage.user.auth.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(
        Long id,
        @JsonProperty("connected_at")
        String connectedAt,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            KakaoProfile profile,
            String email // 비즈 계정에서만 가능
    ) {}

    public record KakaoProfile(
            @JsonProperty("profile_image_url")
            String profileImgUrl
    ) {}

    public String getKakaoId() {
        return id.toString();
    }
}
