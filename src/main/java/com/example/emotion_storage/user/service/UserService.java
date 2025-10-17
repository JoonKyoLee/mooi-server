package com.example.emotion_storage.user.service;

import com.example.emotion_storage.user.auth.oauth.google.GoogleLoginClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleSignUpClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleTokenVerifier;
import com.example.emotion_storage.user.auth.oauth.kakao.KakaoUserInfoClient;
import com.example.emotion_storage.user.auth.oauth.kakao.KakaoUserInfo;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.dto.request.KakaoLoginRequest;
import com.example.emotion_storage.user.dto.request.KakaoSignUpRequest;
import com.example.emotion_storage.user.repository.UserRepository;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String NICKNAME_PATTERN = "^[A-Za-z가-힣]{2,8}$";
    private static final int NOTIFICATION_DEFAULT_HOUR = 21;
    private static final int NOTIFICATION_DEFAULT_MINUTE = 0;

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final KakaoUserInfoClient kakaoUserInfoClient;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
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

    @Transactional
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

        validateNickname(request.nickname());

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
                .keyCount(5L)
                .ticketCount(10L)
                .appPushNotify(true)
                .emotionReminderNotify(true)
                .emotionReminderDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
                .emotionReminderTime(LocalTime.of(NOTIFICATION_DEFAULT_HOUR, NOTIFICATION_DEFAULT_MINUTE))
                .timeCapsuleReportNotify(true)
                .marketingInfoNotify(request.isMarketingAgreed())
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse kakaoLogin(KakaoLoginRequest request, HttpServletResponse response) {
        KakaoUserInfo userInfo = kakaoUserInfoClient.getKakaoUserInfo(request.accessToken());

        User user = userRepository.findByEmail(userInfo.kakaoAccount().email())
                .orElseThrow(() -> new BaseException(ErrorCode.NEED_SIGN_UP));

        if (user.getSocialType().equals(SocialType.GOOGLE)) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED_WITH_GOOGLE);
        }

        String accessToken = tokenService.issueAccessToken(user.getId());
        tokenService.issueRefreshToken(user.getId(), response);

        return new LoginResponse(accessToken);
    }

    @Transactional
    public void kakaoSignUp(KakaoSignUpRequest request) {
        KakaoUserInfo userInfo = kakaoUserInfoClient.getKakaoUserInfo(request.accessToken());

        Optional<User> existingUser = userRepository.findByEmail(userInfo.kakaoAccount().email());
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
                .email(userInfo.kakaoAccount().email())
                .socialId(userInfo.getKakaoId())
                .socialType(SocialType.KAKAO)
                .profileImageUrl(userInfo.kakaoAccount().profile().profileImgUrl())
                .nickname(request.nickname())
                .gender(request.gender())
                .birthday(request.birthday())
                .expectations(request.expectations())
                .isTermsAgreed(request.isTermsAgreed())
                .isPrivacyAgreed(request.isPrivacyAgreed())
                .isMarketingAgreed(request.isMarketingAgreed())
                .keyCount(5L)
                .ticketCount(10L)
                .appPushNotify(true)
                .emotionReminderNotify(true)
                .emotionReminderDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
                .emotionReminderTime(LocalTime.of(NOTIFICATION_DEFAULT_HOUR, NOTIFICATION_DEFAULT_MINUTE))
                .timeCapsuleReportNotify(true)
                .marketingInfoNotify(request.isMarketingAgreed())
                .build();

        userRepository.save(user);
    }

    private void validateNickname(String nickname) {
        if (!Pattern.matches(NICKNAME_PATTERN, nickname)) {
            throw new BaseException(ErrorCode.INVALID_NICKNAME);
        }
    }
}
