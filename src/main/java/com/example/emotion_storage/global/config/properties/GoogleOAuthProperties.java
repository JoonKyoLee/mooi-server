package com.example.emotion_storage.global.config.properties;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.google")
public class GoogleOAuthProperties {

    private String clientIds;

    public void setClientIds(String clientIds) {
        this.clientIds = clientIds;
    }

    public List<String> getClientIdList() {
        if (clientIds == null || clientIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
