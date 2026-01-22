package com.example.emotion_storage.global.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordErrorNotifier {

    private static final int DISCORD_CONTENT_LIMIT = 2000;
    private static final int EXCEPTION_MESSAGE_LIMIT = 300;
    private static final String TRUNC_SUFFIX = "\n...(truncated)";

    private static final String TEMPLATE = """
        🚨 **UNEXPECTED SERVER ERROR**
        - requestId: `%s`
        - request: `%s %s%s`
        - exception: `%s`
        - message: `%s`
        - time: `%s`
        """;

    @Value("${monitoring.discord.error-webhook-url:}")
    private String webhookUrl;

    @Value("${monitoring.discord.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    @Async("discordNotifierExecutor")
    public void notifyUnexpectedException(String requestId, HttpServletRequest request, Throwable exception) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String method = request != null ? safe(request.getMethod(), "UNKNOWN") : "UNKNOWN";
        String path = request != null ? safe(request.getRequestURI(), "UNKNOWN") : "UNKNOWN";

        // 예: ?page=1&size=20 같은 쿼리스트링 전체
        String query = request != null ? request.getQueryString() : null;
        String querySuffix = (query == null || query.isBlank()) ? "" : ("?" + query);

        String exceptionType = exception.getClass().getName();
        String exceptionMessage = safe(exception.getMessage(), "(no message)");
        exceptionMessage = shorten(exceptionMessage);

        String content = TEMPLATE.formatted(
                safe(requestId, "unknown"),
                method,
                path,
                querySuffix,
                exceptionType,
                exceptionMessage,
                LocalDateTime.now()
        );

        sendToDiscord(truncateForDiscord(content));
    }

    private void sendToDiscord(String content) {
        try {
            Map<String, Object> payload = Map.of("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.warn("Discord webhook failed: {}", e.getMessage());
        }
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String shorten(String value) {
        if (value == null) return "";

        if (value.length() <= DiscordErrorNotifier.EXCEPTION_MESSAGE_LIMIT) {
            return value;
        }
        return value.substring(0, DiscordErrorNotifier.EXCEPTION_MESSAGE_LIMIT) + "...";
    }

    private String truncateForDiscord(String content) {
        if (content == null) return "";
        if (content.length() <= DISCORD_CONTENT_LIMIT) return content;

        int max = DISCORD_CONTENT_LIMIT - TRUNC_SUFFIX.length();
        return content.substring(0, Math.max(0, max)) + TRUNC_SUFFIX;
    }
}
