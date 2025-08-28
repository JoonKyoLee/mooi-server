package com.example.emotion_storage.user.auth.oauth.kakao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KakaoConfig {

    @Bean
    public RestClient kakaoRestClient(RestClient.Builder builder) {
        return builder.build();
    }
}
