package com.example.emotion_storage.global.websocket;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.global.security.jwt.JwtTokenProvider;
import com.example.emotion_storage.global.security.jwt.TokenStatus;
import java.security.Principal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        accessor.setLeaveMutable(true);

        if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

            if (!authHeader.startsWith(TOKEN_PREFIX)) {
                throw new BaseException(ErrorCode.UNAUTHORIZED);
            }

            String token = authHeader.substring(TOKEN_PREFIX.length());

            TokenStatus status = jwtTokenProvider.validateToken(token);

            if (status != TokenStatus.VALID) {
                throw new BaseException(status == TokenStatus.EXPIRED
                        ? ErrorCode.ACCESS_TOKEN_EXPIRED
                        : ErrorCode.ACCESS_TOKEN_INVALID);
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // STOMP user를 "Principal(name=userId)"로 통일
            accessor.setUser((Principal) () -> String.valueOf(userId));

            log.info("[STOMP] userId set on {}: {}", accessor.getCommand(), userId);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            log.info("[STOMP] SEND sessionId={}, user={}", accessor.getSessionId(), accessor.getUser());
        }

        // 변경된 accessor를 message에 반영해서 반환
        return MessageBuilder.createMessage(
                message.getPayload(), accessor.getMessageHeaders()
        );
    }
}
