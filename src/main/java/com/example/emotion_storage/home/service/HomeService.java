package com.example.emotion_storage.home.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.home.dto.response.HomeInfoResponse;
import com.example.emotion_storage.home.dto.response.KeyCountResponse;
import com.example.emotion_storage.home.dto.response.NewDailyReportResponse;
import com.example.emotion_storage.home.dto.response.NewNotificationResponse;
import com.example.emotion_storage.home.dto.response.NewTimeCapsuleResponse;
import com.example.emotion_storage.home.dto.response.TicketStatusResponse;
import com.example.emotion_storage.notification.repository.NotificationRepository;
import com.example.emotion_storage.report.repository.ReportRepository;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final Long DAILY_TICKET_LIMIT = 10L;
    
    private final UserRepository userRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;

    public HomeInfoResponse getHomeInfo(Long userId) {
        log.info("사용자 홈 정보 조회 요청 - userId: {}", userId);

        // 사용자 존재 여부 확인
        findUserById(userId);

        // 각종 정보 조회
        TicketStatusResponse ticketStatus = getTicketStatus(userId);
        KeyCountResponse keyCount = getKeyCount(userId);
        NewNotificationResponse notificationStatus = getNewNotificationStatus(userId);
        NewTimeCapsuleResponse timeCapsuleStatus = getNewTimeCapsuleStatus(userId);
        NewDailyReportResponse reportStatus = getNewDailyReportStatus(userId);

        HomeInfoResponse response = HomeInfoResponse.builder()
                .remainingTickets(ticketStatus.getRemainingTickets())
                .dailyLimit(ticketStatus.getDailyLimit())
                .keyCount(keyCount.getKeyCount())
                .hasNewNotification(notificationStatus.isHasNewNotification())
                .notificationCount(notificationStatus.getCount())
                .hasNewTimeCapsule(timeCapsuleStatus.isHasNewCapsule())
                .timeCapsuleCount(timeCapsuleStatus.getCount())
                .hasNewReport(reportStatus.isHasNewReport())
                .reportCount(reportStatus.getCount())
                .build();

        log.info("사용자 홈 정보 조회 완료 - userId: {}, remainingTickets: {}, keyCount: {}, hasNewNotification: {}, hasNewTimeCapsule: {}, hasNewReport: {}",
                userId, response.getRemainingTickets(), response.getKeyCount(),
                response.isHasNewNotification(), response.isHasNewTimeCapsule(), response.isHasNewReport());

        return response;
    }

    public TicketStatusResponse getTicketStatus(Long userId) {
        log.info("사용자 티켓 상태 조회 요청 - userId: {}", userId);
        
        User user = findUserById(userId);
        
        TicketStatusResponse response = TicketStatusResponse.builder()
                .remainingTickets(user.getTicketCount())
                .dailyLimit(DAILY_TICKET_LIMIT)
                .build();
        
        log.info("사용자 티켓 상태 조회 완료 - userId: {}, remainingTickets: {}, dailyLimit: {}", 
                userId, response.getRemainingTickets(), response.getDailyLimit());
        
        return response;
    }

    public KeyCountResponse getKeyCount(Long userId) {
        log.info("사용자 열쇠 개수 조회 요청 - userId: {}", userId);
        
        User user = findUserById(userId);
        
        KeyCountResponse response = KeyCountResponse.builder()
                .keyCount(user.getKeyCount())
                .build();
        
        log.info("사용자 열쇠 개수 조회 완료 - userId: {}, keyCount: {}", 
                userId, response.getKeyCount());
        
        return response;
    }

    public NewTimeCapsuleResponse getNewTimeCapsuleStatus(Long userId) {
        log.info("사용자 도착한 타임캡슐 상태 조회 요청 - userId: {}", userId);
        
        // 사용자 존재 여부 확인
        findUserById(userId);
        
        // 미확인 타임캡슐 개수 조회
        Long unopenedCount = timeCapsuleRepository.countUnopenedTimeCapsulesByUserId(userId);
        boolean hasNewCapsule = unopenedCount > 0;
        
        NewTimeCapsuleResponse response = NewTimeCapsuleResponse.builder()
                .hasNewCapsule(hasNewCapsule)
                .count(unopenedCount.intValue())
                .build();
        
        log.info("사용자 도착한 타임캡슐 상태 조회 완료 - userId: {}, hasNewCapsule: {}, count: {}", 
                userId, response.isHasNewCapsule(), response.getCount());
        
        return response;
    }

    public NewDailyReportResponse getNewDailyReportStatus(Long userId) {
        log.info("사용자 새로운 일일리포트 상태 조회 요청 - userId: {}", userId);
        
        // 사용자 존재 여부 확인
        findUserById(userId);
        
        // 미확인 일일리포트 개수 조회
        Long unopenedReportCount = reportRepository.countUnopenedReportsByUserId(userId);
        boolean hasNewReport = unopenedReportCount > 0;
        
        NewDailyReportResponse response = NewDailyReportResponse.builder()
                .hasNewReport(hasNewReport)
                .count(unopenedReportCount.intValue())
                .build();
        
        log.info("사용자 새로운 일일리포트 상태 조회 완료 - userId: {}, hasNewReport: {}, count: {}", 
                userId, response.isHasNewReport(), response.getCount());
        
        return response;
    }

    public NewNotificationResponse getNewNotificationStatus(Long userId) {
        log.info("사용자 새로운 알림 상태 조회 요청 - userId: {}", userId);
        
        // 사용자 존재 여부 확인
        findUserById(userId);
        
        // 미열람 알림 개수 조회
        Long unreadNotificationCount = notificationRepository.countUnreadNotificationsByUserId(userId);
        boolean hasNewNotification = unreadNotificationCount > 0;
        
        NewNotificationResponse response = NewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .count(unreadNotificationCount.intValue())
                .build();
        
        log.info("사용자 새로운 알림 상태 조회 완료 - userId: {}, hasNew: {}, count: {}", 
                userId, response.isHasNewNotification(), response.getCount());
        
        return response;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
