package com.example.emotion_storage.attendance.service;

import com.example.emotion_storage.attendance.response.AttendanceStreakStatusResponse;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AttendanceStreakStatusResponse getAttendanceRewardStatus(Long userId) {
        User user = findUserById(userId);

        LocalDate today = LocalDate.now();
        LocalDate lastAttendanceRewardDate = user.getLastAttendanceRewardDate();
        int attendanceStreak = user.getAttendanceStreak();
        log.info("사용자 {}의 연속 출석일을 조회합니다.", userId);

        if (lastAttendanceRewardDate == null) {
            log.info("사용자 {}가 출석 보상을 한번도 받지 않았습니다.", userId);
            return new AttendanceStreakStatusResponse(0, false);
        }

        if (lastAttendanceRewardDate.isEqual(today)) {
            log.info("사용자 {}가 이미 출석 보상을 받았습니다.", userId);
            return new AttendanceStreakStatusResponse(attendanceStreak, true);
        }

        if (lastAttendanceRewardDate.isEqual(today.minusDays(1))) {
            log.info("사용자 {}가 어제 출석 보상을 받아 오늘 연속 보상을 받습니다.", userId);
            return new AttendanceStreakStatusResponse(attendanceStreak, false);
        }

        log.info("사용자 {}가 연속 출석을 하지 않았습니다.", userId);
        return new AttendanceStreakStatusResponse(0, false);
    }

    @Transactional
    public AttendanceStreakStatusResponse updateAttendanceRewardStatus(Long userId) {
        User user = findUserById(userId);

        LocalDate today = LocalDate.now();
        LocalDate lastAttendanceRewardDate = user.getLastAttendanceRewardDate();
        int attendanceStreak = user.getAttendanceStreak();

        if (lastAttendanceRewardDate == null) {
            log.info("사용자 {}가 첫 출석 보상을 받았습니다.", userId);
            attendanceStreak = 1;
            user.updateAttendanceStatus(attendanceStreak, today);
            return new AttendanceStreakStatusResponse(attendanceStreak, true);
        }

        if (lastAttendanceRewardDate.isEqual(today)) {
            log.info("사용자 {}가 오늘 출석 보상을 이미 받았습니다.", userId);
            throw new BaseException(ErrorCode.ALREADY_GET_ATTENDANCE_REWARD);
        }

        if (lastAttendanceRewardDate.isEqual(today.minusDays(1))) {
            log.info("사용자 {}가 어제 출석 보상을 받아 오늘 연속 보상을 받았습니다.", userId);
            attendanceStreak = attendanceStreak % 7 + 1;
            user.updateAttendanceStatus(attendanceStreak, today);
            return new AttendanceStreakStatusResponse(attendanceStreak, true);
        }

        log.info("사용자 {}가 연속 출석을 하지 않아 1일차 보상을 받았습니다.", userId);
        attendanceStreak = 1;
        user.updateAttendanceStatus(attendanceStreak, today);
        return new AttendanceStreakStatusResponse(1, true);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
