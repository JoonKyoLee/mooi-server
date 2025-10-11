package com.example.emotion_storage.timecapsule.service;

import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.chat.repository.ChatRoomRepository;
import com.example.emotion_storage.chat.service.ChatService;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.timecapsule.domain.AnalyzedEmotion;
import com.example.emotion_storage.timecapsule.domain.AnalyzedFeedback;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.domain.TimeCapsuleOpenCost;
import com.example.emotion_storage.timecapsule.dto.request.AiTimeCapsuleCreateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleCreateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleFavoriteRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleNoteUpdateRequest;
import com.example.emotion_storage.timecapsule.dto.request.TimeCapsuleSaveRequest;
import com.example.emotion_storage.timecapsule.dto.response.AiTimeCapsuleCreateErrorResponse;
import com.example.emotion_storage.timecapsule.dto.response.AiTimeCapsuleCreateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleCreateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleDetailResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleFavoriteResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleListResponse;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleSaveResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final String SESSION_ID_FORMAT = "session-%d-%d";
    private static final String ARRIVED_STATUS = "arrived";
    private static final String SORT_FAVORITE = "favorite";
    private static final String SORT_BY_TEMP_SAVED = "isTempSave";
    private static final String SORT_BY_DEFAULT_TIME = "historyDate";
    private static final String SORT_BY_ARRIVED_TIME = "openedAt";
    private static final String SORT_BY_FAVORITE_TIME = "favoriteAt";
    private static final int TIME_CAPSULE_FAVORITES_LIMIT = 30;
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    private final ChatService chatService;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.base-url:http://localhost:8000}")
    private String aiServerBaseUrl;

    public TimeCapsuleCreateResponse createTimeCapsule(TimeCapsuleCreateRequest createRequest, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(createRequest.chatroomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        String sessionId = String.format(SESSION_ID_FORMAT, userId, chatRoom.getId());

        try {
            AiTimeCapsuleCreateRequest request = new AiTimeCapsuleCreateRequest(
                    AiTimeCapsuleCreatePrompts.ROLE_MESSAGE,
                    AiTimeCapsuleCreatePrompts.REFERENCE_MESSAGE,
                    AiTimeCapsuleCreatePrompts.ANALYZE_MESSAGE,
                    sessionId
            );

            String url = aiServerBaseUrl + "/timecapsule/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AiTimeCapsuleCreateRequest> requestEntity = new HttpEntity<>(request, headers);

            log.info("AI 서버에 타임캡슐 생성 요청을 전송합니다. URL: {}", url);
            log.info("요청 데이터: {}", objectMapper.writeValueAsString(request));

            ResponseEntity<AiTimeCapsuleCreateResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    AiTimeCapsuleCreateResponse.class
            );

            AiTimeCapsuleCreateResponse responseBody = response.getBody();
            log.info("AI 서버로부터 생성된 타임캡슐 응답을 받았습니다: {}", objectMapper.writeValueAsString(responseBody));

            chatService.closeChatRoom(userId, chatRoom.getId()); // 타임캡슐 생성 후 채팅방 종료

            return TimeCapsuleCreateResponse.from(chatRoom.getFirstChatTime(), responseBody);
        } catch (HttpClientErrorException e) {
            log.error("AI 서버 클라이언트 오류 (4xx): {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 서버 요청 오류: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.error("AI 서버 서버 오류 (5xx): {}", e.getResponseBodyAsString(), e);

            // 500 에러 응답 파싱 시도
            try {
                AiTimeCapsuleCreateErrorResponse errorResponse = objectMapper.readValue(
                        e.getResponseBodyAsString(),
                        AiTimeCapsuleCreateErrorResponse.class
                );
                log.error("AI 서버 에러 상세: {}", errorResponse.detail());
            } catch (Exception parseException) {
                log.warn("에러 응답 파싱 실패: {}", parseException.getMessage());
            }

            throw new RuntimeException("AI 서버 내부 오류: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("타임캡슐 생성 요청 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("타임캡슐 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public TimeCapsuleSaveResponse saveTimeCapsule(TimeCapsuleSaveRequest request, Long userId) {
        // 임시 저장되어 있는 타임캡슐이 존재하는지 확인하고 최종 저장
        if (!request.isTempSave()) {
            log.info("채팅방 {}의 임시 저장되어 있는 타임캡슐이 존재하는지 확인합니다.", request.chatroomId());
            Optional<TimeCapsule> tempTimeCapsule = timeCapsuleRepository
                    .findByChatroomIdAndIsTempSaveTrue(request.chatroomId());

            if (tempTimeCapsule.isPresent()) {
                log.info("임시 저장되어 있는 타임캡슐을 최종 저장합니다.");
                TimeCapsule timeCapsule = tempTimeCapsule.get();
                timeCapsule.updateTempSave(false);
                timeCapsule.setOpenedAt(request.openAt());
                return new TimeCapsuleSaveResponse(timeCapsule.getId());
            }
        }

        log.info("사용자 {}의 채팅방 {}에 대한 타임캡슐을 저장합니다.", userId, request.chatroomId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(request.chatroomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        TimeCapsule timeCapsule = TimeCapsule.builder()
                .user(user)
                .chatroomId(chatRoom.getId())
                .historyDate(chatRoom.getFirstChatTime())
                .oneLineSummary(request.oneLineSummary())
                .dialogueSummary(request.dialogueSummary())
                .myMindNote("")
                .isTempSave(request.isTempSave())
                .isOpened(false)
                .isFavorite(false)
                .openedAt(request.openAt())
                .build();

        request.emotionKeywords().stream()
                .map(emotion -> AnalyzedEmotion.builder()
                        .analyzedEmotion(emotion.label())
                        .percentage(emotion.ratio())
                        .build())
                .forEach(timeCapsule::addAnalyzedEmotion);


        request.aiFeedback().stream()
                .map(feedback -> AnalyzedFeedback.builder()
                        .analyzedFeedback(feedback)
                        .build())
                .forEach(timeCapsule::addAnalyzedFeedback);

        timeCapsuleRepository.save(timeCapsule);
        log.info("타임캡슐 저장에 성공했습니다.");

        return new TimeCapsuleSaveResponse(timeCapsule.getId());
    }

    public TimeCapsuleExistDateResponse getActiveDatesForMonth(
            int year, int month, Long userId
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        log.info("{}년 {}월에 타임캡슐이 존재하는 날짜 목록을 조회합니다.", year, month);
        List<LocalDate> dates =
                timeCapsuleRepository.findActiveDatesInRange(userId, start, end).stream()
                        .map(Date::toLocalDate)
                        .distinct()
                        .toList();

        int activeDays = dates.size();

        return new TimeCapsuleExistDateResponse(activeDays, dates);
    }

    public TimeCapsuleListResponse fetchTimeCapsules(
            LocalDate startDate, LocalDate endDate, int page, int limit, String status, Long userId
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        Pageable pageable;

        if (ARRIVED_STATUS.equals(status)) {
            log.info("사용자 {}의 {}-{}의 도착한 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            pageable = pageDesc(page, limit, SORT_BY_ARRIVED_TIME);
            LocalDateTime end = LocalDateTime.now();
            return fetchArrivedTimeCapsules(start, end, page, limit, userId, pageable);
        } else {
            log.info("사용자 {}의 {}-{}의 타임캡슐 목록을 조회합니다.", userId, startDate, endDate);
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            return fetchTimeCapsulesByDateRange(start, end, page, limit, userId);
        }
    }

    public TimeCapsuleListResponse fetchFavoriteTimeCapsules(
            int page, int limit, String sort, Long userId
    ) {
        final boolean sortFavorite = SORT_FAVORITE.equals(sort);
        Pageable pageable = pageDesc(page, limit, sortFavorite ? SORT_BY_FAVORITE_TIME : SORT_BY_DEFAULT_TIME);

        log.info("사용자 {}의 즐겨찾기 리스트를 조회합니다.", userId);
        return fetchFavoritesTimeCapsulesPage(page, limit, userId, pageable);
    }

    private Pageable pageDesc(int page, int limit, String sortField) {
        int zeroBasedPage = Math.max(0, page - 1);
        return PageRequest.of(zeroBasedPage, limit, Sort.by(sortField).descending());
    }

    private TimeCapsuleListResponse fetchTimeCapsulesByDateRange(
            LocalDateTime start, LocalDateTime end, int page, int limit, Long userId
    ) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(
                zeroBasedPage, limit,
                Sort.by(SORT_BY_TEMP_SAVED).descending().and(Sort.by(SORT_BY_DEFAULT_TIME).descending())
        );
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndHistoryDateBetween(
                        userId, start, end, pageable
                );
        return TimeCapsuleListResponse.of(timeCapsules, page, limit);
    }

    private TimeCapsuleListResponse fetchArrivedTimeCapsules(
            LocalDateTime start, LocalDateTime end, int page, int limit, Long userId, Pageable pageable
    ) {
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndIsOpenedFalseAndOpenedAtGreaterThanEqualAndOpenedAtLessThanEqual(
                        userId, start, end, pageable
                );
        return TimeCapsuleListResponse.of(timeCapsules, page, limit);
    }

    private TimeCapsuleListResponse fetchFavoritesTimeCapsulesPage(
            int page, int limit, Long userId, Pageable pageable
    ) {
        Page<TimeCapsule> timeCapsules =
                timeCapsuleRepository.findByUser_IdAndDeletedAtIsNullAndIsFavoriteIsTrue(userId, pageable);
        return TimeCapsuleListResponse.of(timeCapsules, page, limit);
    }

    private TimeCapsule findOwnedTimeCapsule(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = timeCapsuleRepository.findById(timeCapsuleId)
                .orElseThrow(() -> new BaseException(ErrorCode.TIME_CAPSULE_NOT_FOUND));

        if (!timeCapsule.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_IS_NOT_OWNED);
        }
        return timeCapsule;
    }

    @Transactional
    public TimeCapsuleFavoriteResponse setFavorite(
            Long timeCapsuleId, TimeCapsuleFavoriteRequest request, Long userId
    ) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        if (request.addFavorite()) {
            validateFavoriteLimit(userId);
            log.info("타임캡슐 {}을 즐겨찾기 목록에 추가합니다.", timeCapsule.getId());
            timeCapsule.markFavorite();

        } else {
            log.info("타임캡슐 {}을 즐겨찾기 목록에서 해제합니다.", timeCapsule.getId());
            timeCapsule.unmarkFavorite();
        }

        return new TimeCapsuleFavoriteResponse(
                timeCapsule.getIsFavorite(),
                timeCapsule.getFavoriteAt(),
                timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(userId)
        );
    }

    private void validateFavoriteLimit(Long userId) {
        log.info("즐겨찾기된 타임캡슐 개수를 조회합니다.");
        int favoriteCnt = timeCapsuleRepository.countByUser_IdAndIsFavoriteTrue(userId);
        if (favoriteCnt >= TIME_CAPSULE_FAVORITES_LIMIT) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_FAVORITE_LIMIT_EXCEEDED);
        }
    }

    public TimeCapsuleDetailResponse getTimeCapsuleDetail(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);
        log.info("타임캡슐 {}에 대한 상세 정보를 조회합니다.", timeCapsuleId);
        return TimeCapsuleDetailResponse.from(timeCapsule);
    }

    @Transactional
    public void openTimeCapsule(Long timeCapsuleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        LocalDateTime openDate = timeCapsule.getOpenedAt();
        long days = calculateDaysToOpen(openDate);

        if (days != 0) {
            consumeKeysForOpening(user, days);
        }

        timeCapsule.setIsOpened(true);
    }

    private long calculateDaysToOpen(LocalDateTime openDate) {
        log.info("타임캡슐을 열 때까지 필요한 날을 계산합니다.");

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(openDate)) {
            return 0;
        }

        long seconds = Duration.between(now, openDate).getSeconds();
        return (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
    }

    private void consumeKeysForOpening(User user, long days) {
        log.info("타임캡슐을 열 때 필요한 열쇠의 개수를 계산합니다.");
        long requiredKeys = TimeCapsuleOpenCost.getRequiredKeys(days);
        user.consumeKeys(requiredKeys);
        log.info("열쇠 {}개를 사용했습니다. 남은 열쇠의 개수는 {}개입니다.", requiredKeys, user.getKeyCount());
    }

    @Transactional
    public void updateMindNote(Long timeCapsuleId, TimeCapsuleNoteUpdateRequest request, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        log.info("타임캡슐 {}의 내 마음 노트를 업데이트 합니다.", timeCapsuleId);
        timeCapsule.updateMyMindNote(request.content());
    }

    @Transactional
    public void deleteTimeCapsule(Long timeCapsuleId, Long userId) {
        TimeCapsule timeCapsule = findOwnedTimeCapsule(timeCapsuleId, userId);

        log.info("타임캡슐 {}를 삭제합니다.", timeCapsuleId);
        timeCapsule.setDeletedAt(LocalDateTime.now());
    }
}
