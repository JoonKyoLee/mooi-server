package com.example.emotion_storage.report.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.report.dto.response.DailyReportDetailResponse;
import com.example.emotion_storage.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "일일리포트", description = "일일리포트 관련 API")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily-report")
    @Operation(summary = "일일리포트 상세 조회", description = "특정 날짜의 일일리포트 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일일리포트 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<DailyReportDetailResponse>> getDailyReportDetail(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", required = true, example = "2025-09-01")
            @RequestParam String date,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("일일리포트 상세 조회 API 호출 - userId: {}, date: {}", userId, date);
        
        DailyReportDetailResponse response = reportService.getDailyReportDetail(userId, date);
        
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.DAILY_REPORT_DETAIL_GET_SUCCESS.getMessage(), response)
        );
    }

    @GetMapping("/daily-report/{reportId}")
    @Operation(summary = "아이디 기반 일일리포트 상세 조회", description = "특정 날짜의 일일리포트 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DailyReportDetailResponse>> getDailyReportDetailById(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("아이디 기반 일일리포트 상세 조회 API 호출 - userId: {}, reportId: {}", userId, reportId);
        DailyReportDetailResponse response = reportService.getDailyReportDetailById(userId, reportId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessage.DAILY_REPORT_DETAIL_GET_SUCCESS.getMessage(), response)
        );
    }
}
