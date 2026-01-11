package com.example.emotion_storage.global.websocket;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.global.security.jwt.JwtTokenProvider;
import com.example.emotion_storage.global.security.jwt.TokenStatus;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

            if (!authHeader.startsWith(TOKEN_PREFIX)) {
                throw new BaseException(ErrorCode.UNAUTHORIZED);
            }

            String token = authHeader.substring(TOKEN_PREFIX.length());

            TokenStatus status = jwtTokenProvider.validateToken(token);

            if (status == TokenStatus.VALID) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

                List<GrantedAuthority> authorities = Collections.emptyList();
                CustomUserPrincipal principal =
                        new CustomUserPrincipal(user.getId(), user.getEmail(), authorities);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                accessor.setUser(authenticationToken);
                log.info("[STOMP] 유저 추출 성공 user: {}", accessor.getUser());
            } else {
                throw new BaseException(status == TokenStatus.EXPIRED
                        ? ErrorCode.ACCESS_TOKEN_EXPIRED
                        : ErrorCode.ACCESS_TOKEN_INVALID);
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            log.info("[STOMP] SEND sessionId={}, user={}", accessor.getSessionId(), accessor.getUser());
        }

        return message;
    }
}
