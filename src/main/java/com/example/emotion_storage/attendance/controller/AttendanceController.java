package com.example.emotion_storage.attendance.controller;

import com.example.emotion_storage.attendance.response.AttendanceStreakStatusResponse;
import com.example.emotion_storage.attendance.service.AttendanceService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "출석 관련 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "출석 보상 현황 조회", description = "사용자의 출석 보상 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<AttendanceStreakStatusResponse>> getAttendanceRewardStatus(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 출석 보상 현황 조회를 요청했습니다.", userId);
        AttendanceStreakStatusResponse response = attendanceService.getAttendanceRewardStatus(userId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.GET_ATTENDANCE_STATUS_SUCCESS.getMessage(), response)
        );
    }
}
