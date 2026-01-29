package com.example.emotion_storage.global.config.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthPropertiesConfig {
}
