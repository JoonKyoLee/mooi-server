package com.example.emotion_storage.mypage.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.request.NotificationSettingsUpdateRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.user.auth.service.TokenService;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public MyPageOverviewResponse getMyPageOverview(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 닉네임, 가입 일수, 열쇠 개수를 조회합니다.", userId);

        LocalDate signupDate = user.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(signupDate, today) + 1; // 가입한 순간부터 1일로 계산

        return new MyPageOverviewResponse(user.getNickname(), totalDays, user.getKeyCount());
    }

    @Transactional
    public void changeUserNickname(NicknameChangeRequest request, Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 닉네임을 수정합니다.", userId);
        user.updateNickname(request.nickname());
    }

    @Transactional(readOnly = true)
    public UserAccountInfoResponse getUserAccountInfo(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 계정 정보를 조회합니다.", userId);
        return new UserAccountInfoResponse(
                user.getEmail(), user.getSocialType(), user.getGender(), user.getBirthday()
        );
    }

    @Transactional(readOnly = true)
    public NotificationSettingsResponse getNotificationSettings(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 알림 설정 상태 정보를 조회합니다.", userId);
        return new NotificationSettingsResponse(
                user.isAppPushNotify(),
                user.isEmotionReminderNotify(),
                user.getEmotionReminderDays(),
                user.getEmotionReminderTime(),
                user.isTimeCapsuleReportNotify(),
                user.isMarketingInfoNotify()
        );
    }

    @Transactional
    public void updateNotificationSettings(NotificationSettingsUpdateRequest request, Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 알림 설정 상태를 업데이트합니다.", userId);

        user.updateAppPushNotify(request.appPushNotify());
        user.updateEmotionReminder(request.emotionReminderNotify(), request.emotionReminderDays(), request.emotionReminderTime());
        user.updateTimeCapsuleReportNotify(request.timeCapsuleReportNotify());
        user.updateMarketingInfoNotify(request.marketingInfoNotify());
    }

    @Transactional
    public void withdrawUser(Long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = findUserById(userId);

        log.info("회원 {}의 탈퇴 처리를 진행합니다.", userId);
        user.withdrawUser();

        logout(userId, request, response);
    }

    @Transactional
    public void logout(Long userId, HttpServletRequest request, HttpServletResponse response) {
        log.info("회원 {}의 로그아웃을 진행합니다.", userId);
        tokenService.revokeTokens(request, response, userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
