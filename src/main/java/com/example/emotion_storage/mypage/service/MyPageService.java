package com.example.emotion_storage.mypage.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.request.NotificationSettingsUpdateRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.PolicyResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public MyPageOverviewResponse getMyPageOverview(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 닉네임, 가입 일수, 열쇠 개수를 조회합니다.", userId);

        LocalDateTime signupDate = user.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long totalDays = ChronoUnit.DAYS.between(signupDate, now);

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

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy() {
        return new PolicyResponse("정책"); // 약관 및 개인정보 처리 방침은 논의 필요
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = findUserById(userId);

        log.info("회원 {}의 탈퇴 처리를 진행합니다.", userId);
        user.withdrawUser();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
