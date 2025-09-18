package com.example.emotion_storage.timecapsule.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.service.TimeCapsuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/time-capsule")
@RequiredArgsConstructor
@Tag(name = "TimeCapsule", description = "타임캡슐 관련 API")
public class TimeCapsuleController {

    private final TimeCapsuleService timeCapsuleService;

    @GetMapping("/date")
    @Operation(summary = "타임캡슐 존재하는 날짜 조회", description = "yyyy년 MM월의 날짜 중 캡슐이 존재하는 날짜 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleExistDateResponse>> getMonthlyActiveDates(
            @RequestParam int year, @RequestParam int month, @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 {}년 {}월의 타임캡슐 목록 조회를 요청받았습니다.", userId, year, month);
        ApiResponse<TimeCapsuleExistDateResponse> response = timeCapsuleService.getMonthlyActiveDates(year, month, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation()
    public ResponseEntity<ApiResponse<TimeCapsuleListResponse>> getTimeCapsuleList(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam int page, @RequestParam int limit, @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 타임캡슐 목록 조회를 요청받았습니다.", userId);
        ApiResponse<TimeCapsuleListResponse> response =
                timeCapsuleService.getTimeCapsuleList(startDate, endDate, page, limit, status, userId);
        return ResponseEntity.ok(response);
    }
}
