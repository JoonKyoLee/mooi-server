package com.example.emotion_storage.user.withdrawal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "withdrawal")
public record WithdrawalProperties(int purgeAfterMonths) {
}
