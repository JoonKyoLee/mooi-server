package com.example.emotion_storage.user.auth.oauth.google;

import com.example.emotion_storage.global.config.properties.GoogleOAuthProperties;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleOAuthProperties googleOAuthProperties;

    public GoogleLoginClaims verifyLoginToken(String idToken) {
        Payload payload = verifyToken(idToken);
        return new GoogleLoginClaims(payload.getEmail());
    }

    public GoogleSignUpClaims verifySignUpToken(String idToken) {
        Payload payload = verifyToken(idToken);
        return new GoogleSignUpClaims(
                payload.getSubject(), payload.getEmail(), (String) payload.get("picture")
        );
    }

    public Payload verifyToken(String idToken) {
        try {
            List<String> clientIds = googleOAuthProperties.getClientIdList();
            if (clientIds.isEmpty()) {
                throw new BaseException(ErrorCode.INVALID_ID_TOKEN);
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(clientIds)
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new BaseException(ErrorCode.INVALID_ID_TOKEN);
            }

            return googleIdToken.getPayload();
        } catch (IOException | GeneralSecurityException e) {
            throw new BaseException(ErrorCode.INVALID_ID_TOKEN);
        }
    }
}
