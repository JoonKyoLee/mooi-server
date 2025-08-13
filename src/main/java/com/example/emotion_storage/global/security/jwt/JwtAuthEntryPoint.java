package com.example.emotion_storage.global.security.jwt;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorCode errorCode = (ErrorCode) request.getAttribute("authErrorCode");
        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        ApiResponse<Void> body = ApiResponse.fail(errorCode);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setHeader(
                "WWW-Authenticate",
                "Bearer error=\"invalid_token\", error_description=\"" + errorCode.name() + "\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
