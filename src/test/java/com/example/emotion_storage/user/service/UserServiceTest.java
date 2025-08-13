package com.example.emotion_storage.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.auth.oauth.google.GoogleLoginClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleSignUpClaims;
import com.example.emotion_storage.user.auth.oauth.google.GoogleTokenVerifier;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import com.example.emotion_storage.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private TokenService tokenService;
    @Mock private HttpServletResponse httpServletResponse;

    private GoogleLoginRequest createGoogleLoginRequest() {
        return new GoogleLoginRequest("id-token");
    }

    private GoogleLoginRequest createErrorGoogleLoginRequest() {
        return new GoogleLoginRequest("error-token");
    }

    private GoogleLoginClaims createGoogleLoginClaims() {
        return new GoogleLoginClaims("test@email.com");
    }

    private GoogleLoginClaims createErrorGoogleLoginClaims() {
        return new GoogleLoginClaims("error@email.com");
    }

    private User createGoogleLoginUser(SocialType socialType) {
        return User.builder()
                .id(1L)
                .email("test@email.com")
                .socialType(socialType)
                .build();
    }

    private GoogleSignUpRequest createGoogleSignUpRequest() {
        return new GoogleSignUpRequest(
                "모이",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                List.of("내 감정을 정리하고 싶어요", "내 감정 패턴을 알고 싶어요"),
                true,
                true,
                false,
                "id-token"
        );
    }

    private GoogleSignUpRequest createErrorGoogleSignUpRequest() {
        return new GoogleSignUpRequest(
                "모이",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                List.of("내 감정을 정리하고 싶어요", "내 감정 패턴을 알고 싶어요"),
                true,
                true,
                false,
                "error-token"
        );
    }

    private GoogleSignUpClaims createGoogleSignUpClaims() {
        return new GoogleSignUpClaims(
                "google-social-id",
                "test@email.com",
                "https://example.com/profile.jpg"
        );

    }

    private User createGoogleSignUpUser(GoogleSignUpRequest request, GoogleSignUpClaims claims, SocialType socialType) {
        return User.builder()
                .email(claims.email())
                .socialId(claims.subject())
                .socialType(socialType)
                .profileImageUrl(claims.profileImgUrl())
                .nickname(request.nickname())
                .birthday(request.birthday())
                .expectations(request.expectations())
                .isTermsAgreed(request.isTermsAgreed())
                .isPrivacyAgreed(request.isPrivacyAgreed())
                .isMarketingAgreed(request.isMarketingAgreed())
                .build();
    }

    @Test
    void 구글_로그인에_성공하면_토큰을_반환한다() {
        // given
        GoogleLoginRequest request = createGoogleLoginRequest();
        GoogleLoginClaims claims = createGoogleLoginClaims();

        User user = createGoogleLoginUser(SocialType.GOOGLE);

        when(googleTokenVerifier.verifyLoginToken("id-token")).thenReturn(claims);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(tokenService.issueAccessToken(1L)).thenReturn("access-token");

        // when
        LoginResponse response = userService.googleLogin(request, httpServletResponse);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void 회원가입_하지_않은_구글_계정으로_로그인을_시도할_때_예외가_발생한다() {
        // given
        GoogleLoginRequest request = createGoogleLoginRequest();
        GoogleLoginClaims claims = createErrorGoogleLoginClaims();

        when(googleTokenVerifier.verifyLoginToken("id-token")).thenReturn(claims);
        when(userRepository.findByEmail("error@email.com")).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userService.googleLogin(request, httpServletResponse))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.NEED_SIGN_UP.getMessage());
    }

    @Test
    void 카카오_계정으로_가입되어_있는_이메일로_구글_로그인을_시도할_때_예외가_발생한다() {
        // given
        GoogleLoginRequest request = createGoogleLoginRequest();
        GoogleLoginClaims claims = createGoogleLoginClaims();

        User user = createGoogleLoginUser(SocialType.KAKAO);

        when(googleTokenVerifier.verifyLoginToken("id-token")).thenReturn(claims);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> userService.googleLogin(request, httpServletResponse))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO.getMessage());
    }

    @Test
    void 구글_로그인을_시도할_때_아이디_토큰이_유효하지_않을_때_예외가_발생한다() {
        // given
        GoogleLoginRequest request = createErrorGoogleLoginRequest();

        when(googleTokenVerifier.verifyLoginToken("error-token")).thenThrow(new BaseException(ErrorCode.INVALID_ID_TOKEN));

        // when, then
        assertThatThrownBy(() -> userService.googleLogin(request, httpServletResponse))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.INVALID_ID_TOKEN.getMessage());
    }

    @Test
    void 구글_회원가입에_성공하면_회원이_저장된다() {
        // given
        GoogleSignUpRequest request = createGoogleSignUpRequest();
        GoogleSignUpClaims claims = createGoogleSignUpClaims();

        when(googleTokenVerifier.verifySignUpToken(request.idToken())).thenReturn(claims);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        // when
        userService.googleSignUp(request);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 구글_회원가입한_이메일로_구글_회원가입을_시도할_때_예외가_발생한다() {
        /// given
        GoogleSignUpRequest request = createGoogleSignUpRequest();
        GoogleSignUpClaims claims = createGoogleSignUpClaims();

        User user = createGoogleSignUpUser(request, claims, SocialType.GOOGLE);

        when(googleTokenVerifier.verifySignUpToken(request.idToken())).thenReturn(claims);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> userService.googleSignUp(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.ALREADY_REGISTERED_WITH_GOOGLE.getMessage());
    }

    @Test
    void 카카오_회원가입한_이메일로_구글_회원가입을_시도할_때_예외가_발생한다() {
        /// given
        GoogleSignUpRequest request = createGoogleSignUpRequest();
        GoogleSignUpClaims claims = createGoogleSignUpClaims();

        User user = createGoogleSignUpUser(request, claims, SocialType.KAKAO);

        when(googleTokenVerifier.verifySignUpToken(request.idToken())).thenReturn(claims);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> userService.googleSignUp(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.ALREADY_REGISTERED_WITH_KAKAO.getMessage());
    }

    @Test
    void 구글_회원가입을_시도할_때_아이디_토큰이_유효하지_않을_때_예외가_발생한다() {
        // given
        GoogleSignUpRequest request = createErrorGoogleSignUpRequest();

        when(googleTokenVerifier.verifySignUpToken("error-token")).thenThrow(new BaseException(ErrorCode.INVALID_ID_TOKEN));

        // when, then
        assertThatThrownBy(() -> userService.googleSignUp(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(ErrorCode.INVALID_ID_TOKEN.getMessage());
    }
}
