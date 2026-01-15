package com.example.emotion_storage.timecapsule.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.global.security.principal.CustomUserPrincipal;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleCreateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleFavoriteRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleNoteUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleOpenDateUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleSaveRequest;
import com.example.emotion_storage.timecapsule.dto.response.ArrivedTimeCapsuleCountResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleCreateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleDetailResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleFavoriteResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleOpenDateUpdateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleSaveResponse;
import com.example.emotion_storage.timecapsule.service.TimeCapsuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/create")
    @Operation(summary = "타임캡슐 생성", description = "대화를 기반으로 타임캡슐을 생성합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleCreateResponse>> createTimeCapsule(
            @RequestBody TimeCapsuleCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 채팅방 {}에 대한 타임캡슐을 생성합니다.", userId, request.chatroomId());
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.CREATE_TIME_CAPSULE_SUCCESS.getMessage(),
                timeCapsuleService.createTimeCapsule(request, userId)
        ));
    }

    @PostMapping("/{capsuleId}/openAt")
    public ResponseEntity<ApiResponse<TimeCapsuleOpenDateUpdateResponse>> updateTimeCapsuleOpenDate(
            @PathVariable("capsuleId") Long timeCapsuleId,
            @RequestBody TimeCapsuleOpenDateUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {}에 대한 오픈일 저장을 요청했습니다.", userId, timeCapsuleId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.UPDATE_TIME_CAPSULE_OPEN_DATE_SUCCESS.getMessage(),
                timeCapsuleService.updateTimeCapsuleOpenDate(request, userId)
        ));
    }

    @GetMapping("/date")
    @Operation(summary = "타임캡슐 존재하는 날짜 조회", description = "yyyy년 MM월의 날짜 중 캡슐이 존재하는 날짜 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleExistDateResponse>> getMonthlyActiveDates(
            @RequestParam int year, @RequestParam int month, @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 {}년 {}월의 타임캡슐 목록 조회를 요청받았습니다.", userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage(),
                timeCapsuleService.getActiveDatesForMonth(year, month, userId)
        ));
    }

    @GetMapping
    @Operation(summary = "타임캡슐 목록 조회", description = "날짜 및 타임캡슐 상태를 기준으로 타임캡슐 목록을 조회하고 반환합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleListResponse>> getTimeCapsuleList(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam int page, @RequestParam int limit, @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 타임캡슐 목록 조회를 요청받았습니다.", userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_TIME_CAPSULE_LIST_SUCCESS.getMessage(),
                timeCapsuleService.fetchTimeCapsules(startDate, endDate, page, limit, status, userId)
        ));
    }

    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기 타임캡슐 목록 조회", description = "즐겨찾기한 타임캡슐을 최신순 또는 즐겨찾기한 순으로 조회합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleListResponse>> getFavoriteTimeCapsules(
            @RequestParam int page, @RequestParam int limit, @RequestParam String sort, @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}의 즐겨찾기 타임캡슐 목록 조회를 요청받았습니다.", userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_FAVORITE_TIME_CAPSULE_LIST_SUCCESS.getMessage(),
                timeCapsuleService.fetchFavoriteTimeCapsules(page, limit, sort, userId)
        ));
    }

    @PatchMapping("{capsuleId}/favorite")
    @Operation(summary = "즐겨찾기 추가 및 해제", description = "타임캡슐을 즐겨찾기 등록하거나 해제합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleFavoriteResponse>> setFavorite(
            @PathVariable("capsuleId") Long timeCapsuleId,
            @RequestBody TimeCapsuleFavoriteRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {} 즐겨찾기 변경을 요청했습니다.", userId, timeCapsuleId);

        SuccessMessage successMessage = request.addFavorite()
                ? SuccessMessage.ADD_FAVORITE_TIME_CAPSULE_SUCCESS
                : SuccessMessage.REMOVE_FAVORITE_TIME_CAPSULE_SUCCESS;

        return ResponseEntity.ok(ApiResponse.success(
                successMessage.getMessage(),
                timeCapsuleService.setFavorite(timeCapsuleId, request, userId)
        ));
    }

    @PatchMapping("{capsuleId}/open")
    @Operation(summary = "타임캡슐 열람", description = "타임캡슐을 열람 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> openTimeCapsule(
            @PathVariable("capsuleId") Long timeCapsuleId, @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {} 열람을 요청했습니다.", userId, timeCapsuleId);
        timeCapsuleService.openTimeCapsule(timeCapsuleId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.NO_CONTENT.value(),
                SuccessMessage.OPEN_TIME_CAPSULE_SUCCESS.getMessage(),
                null
        ));
    }

    @GetMapping("{capsuleId}")
    @Operation(summary = "타임캡슐 상세 조회", description = "타임캡슐의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TimeCapsuleDetailResponse>> getTimeCapsuleDetail(
            @PathVariable("capsuleId") Long timeCapsuleId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {}의 상세 조회를 요청했습니다.", userId, timeCapsuleId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_TIME_CAPSULE_DETAIL_SUCCESS.getMessage(),
                timeCapsuleService.getTimeCapsuleDetail(timeCapsuleId, userId)
        ));
    }

    @PatchMapping("{capsuleId}/note")
    @Operation(summary = "타임캡슐 마음노트 수정", description = "타임캡슐 마음노트를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateTimeCapsuleNote(
            @PathVariable("capsuleId") Long timeCapsuleId,
            @RequestBody TimeCapsuleNoteUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {}의 마음노트 수정을 요청했습니다.", userId, timeCapsuleId);
        timeCapsuleService.updateMindNote(timeCapsuleId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.NO_CONTENT.value(),
                SuccessMessage.UPDATE_TIME_CAPSULE_MIND_NOTE_SUCCESS.getMessage(),
                null
        ));
    }

    @DeleteMapping("{capsuleId}")
    @Operation(summary = "타임캡슐 삭제", description = "타임캡슐을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteTimeCapsule(
            @PathVariable("capsuleId") Long timeCapsuleId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 타임캡슐 {} 삭제를 요청했습니다.", userId, timeCapsuleId);
        timeCapsuleService.deleteTimeCapsule(timeCapsuleId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.NO_CONTENT.value(),
                SuccessMessage.DELETE_TIME_CAPSULE_SUCCESS.getMessage(),
                null
        ));
    }

    @GetMapping("/arrived/count")
    @Operation(summary = "최근 3주 내 도착 타임캡슐 개수 조회", description = "최근 3주 내에 도착한 미열람 타임캡슐 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<ArrivedTimeCapsuleCountResponse>> getArrivedTimeCapsuleCount(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 1L; // TODO: 개발 테스트를 위한 코드
        log.info("사용자 {}가 최근 3주 이내 도착한 타임캡슐 개수를 요청했습니다.", userId);
        ArrivedTimeCapsuleCountResponse response = timeCapsuleService.getArrivedTimeCapsuleCount(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessMessage.GET_ARRIVED_TIME_CAPSULE_COUNT_SUCCESS.getMessage(), response
        ));
    }
}
