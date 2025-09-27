package com.example.emotion_storage.mypage.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.mypage.dto.response.UserInfoResponse;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        log.info("사용자 {}의 닉네임, 가입 일수, 열쇠 개수를 조회합니다.", userId);
        String nickname = user.getNickname();
        long keys = user.getKeyCount();

        LocalDateTime signupDate = user.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long totalDays = ChronoUnit.DAYS.between(signupDate, now);

        return new UserInfoResponse(nickname, totalDays, keys);
    }
}
