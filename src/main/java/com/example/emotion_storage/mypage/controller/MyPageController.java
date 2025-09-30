package com.example.emotion_storage.mypage.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.response.MyPageOverviewResponse;
import com.example.emotion_storage.mypage.dto.response.NotificationSettingsResponse;
import com.example.emotion_storage.mypage.dto.response.UserAccountInfoResponse;
import com.example.emotion_storage.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    @Operation(summary = "먀이페이지 초기 화면 조회 API", description = "닉네임, 가입 기간, 보유 열쇠를 반환합니다.")
    public ResponseEntity<ApiResponse<MyPageOverviewResponse>> getMyPageOverview(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 마이페이지 내 정보 조회를 요청받았습니다.", userId);
        MyPageOverviewResponse response = myPageService.getMyPageOverview(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_MY_PAGE_USER_INFO_SUCCESS.getMessage(), response));
    }

    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정 API", description = "사용자의 닉네임을 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> changeUserNickname(
            @RequestBody NicknameChangeRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 닉네임 수정을 요청받았습니다.", userId);
        myPageService.changeUserNickname(request, userId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.NO_CONTENT.value(),
                SuccessMessage.CHANGE_USER_NICKNAME_SUCCESS.getMessage(),
                null));
    }

    @GetMapping("/profile")
    @Operation(summary = "사용자 계정 정보 조회 API", description = "이메일, 연동 정보 등을 반환합니다.")
    public ResponseEntity<ApiResponse<UserAccountInfoResponse>> getUserAccountInfo(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 계정 정보 조회를 요청받았습니다.", userId);
        UserAccountInfoResponse response = myPageService.getUserAccountInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_USER_ACCOUNT_INFO_SUCCESS.getMessage(), response));
    }

    @GetMapping("/notification-settings")
    @Operation(summary = "사용자 알림설정 상태 정보 조회 API", description = "사용자가 알림설정 상태를 설정한 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 알림설정 상태 정보 조회를 요청받았습니다.", userId);
        NotificationSettingsResponse response = myPageService.getNotificationSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_NOTIFICATION_SETTINGS_SUCCESS.getMessage(), response));
    }
}
