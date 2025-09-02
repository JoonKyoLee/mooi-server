package com.example.emotion_storage.user.service;

import com.example.emotion_storage.user.auth.oauth.google.GoogleLoginClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleSignUpClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleTokenVerifier;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.repository.UserRepository;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenService tokenService;

    public LoginResponse googleLogin(GoogleLoginRequest request, HttpServletResponse response) {
        GoogleLoginClaims claims = googleTokenVerifier.verifyLoginToken(request.idToken());

        User user = userRepository.findByEmail(claims.email())
                .orElseThrow(() -> new BaseException(ErrorCode.NEED_SIGN_UP));

        if (user.getSocialType().equals(SocialType.KAKAO)) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO);
        }

        String accessToken = tokenService.issueAccessToken(user.getId());
        tokenService.issueRefreshToken(user.getId(), response);

        return new LoginResponse(accessToken);
    }

    public void googleSignUp(GoogleSignUpRequest request) {
        GoogleSignUpClaims claims = googleTokenVerifier.verifySignUpToken(request.idToken());

        Optional<User> existingUser = userRepository.findByEmail(claims.email());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getSocialType().equals(SocialType.GOOGLE)) {
                throw new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_GOOGLE);
            }
            if (user.getSocialType().equals(SocialType.KAKAO)) {
                throw new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO);
            }
        }

        User user = User.builder()
                .email(claims.email())
                .socialId(claims.subject())
                .socialType(SocialType.GOOGLE)
                .profileImageUrl(claims.profileImgUrl())
                .nickname(request.nickname())
                .gender(request.gender())
                .birthday(request.birthday())
                .expectations(request.expectations())
                .isTermsAgreed(request.isTermsAgreed())
                .isPrivacyAgreed(request.isPrivacyAgreed())
                .isMarketingAgreed(request.isMarketingAgreed())
                .build();

        userRepository.save(user);
    }
}
