package com.example.emotion_storage.user.auth.oauth.kakao;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class KakaoUserInfoClient {

    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String BEARER_HEADER_FORMAT = "Bearer %s";

    private final RestClient restClient;

    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        try {
            KakaoUserInfo userInfo = restClient.get()
                    .uri(KAKAO_USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, makeBearerFormat(accessToken))
                    .retrieve()
                    .body(KakaoUserInfo.class);

            if (userInfo.id() == null) {
                throw new BaseException(ErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
            }

            return userInfo;

        } catch (RestClientResponseException e) {
            throw new BaseException(ErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
        }
    }

    private String makeBearerFormat(String accessToken) {
        return String.format(BEARER_HEADER_FORMAT, accessToken);
    }
}
