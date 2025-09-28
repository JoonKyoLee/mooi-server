package com.example.emotion_storage.mypage.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.mypage.dto.request.NicknameChangeRequest;
import com.example.emotion_storage.mypage.dto.response.UserInfoResponse;
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
    @Operation(summary = "유저 정보 API", description = "닉네임, 가입 기간, 보유 열쇠를 반환합니다.")
    public ResponseEntity<ApiResponse<UserInfoResponse>> createChatRoomForTest(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 마이페이지 내 정보 조회를 요청받았습니다.", userId);
        UserInfoResponse response = myPageService.getUserInfo(userId);
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
}
