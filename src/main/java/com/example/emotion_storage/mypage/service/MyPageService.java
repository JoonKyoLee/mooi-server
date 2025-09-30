package com.example.emotion_storage.mypage.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageOverviewResponse getMyPageOverview(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 닉네임, 가입 일수, 열쇠 개수를 조회합니다.", userId);

        LocalDateTime signupDate = user.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long totalDays = ChronoUnit.DAYS.between(signupDate, now);

        return new MyPageOverviewResponse(user.getNickname(), totalDays, user.getKeyCount());
    }

    public void changeUserNickname(NicknameChangeRequest request, Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 닉네임을 수정합니다.", userId);
        user.updateNickname(request.nickname());
    }

    public UserAccountInfoResponse getUserAccountInfo(Long userId) {
        User user = findUserById(userId);

        log.info("사용자 {}의 계정 정보를 조회합니다.", userId);
        return new UserAccountInfoResponse(
                user.getEmail(), user.getSocialType(), user.getGender(), user.getBirthday()
        );
    }

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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
