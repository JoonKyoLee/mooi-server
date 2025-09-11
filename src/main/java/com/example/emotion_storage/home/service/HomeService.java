package com.example.emotion_storage.home.service;

import com.example.emotion_storage.home.dto.response.TicketStatusResponse;
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

    public TicketStatusResponse getTicketStatus(Long userId) {
        log.info("사용자 티켓 상태 조회 요청 - userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId));
        
        TicketStatusResponse response = TicketStatusResponse.builder()
                .remainingTickets(user.getTicketCount())
                .dailyLimit(DAILY_TICKET_LIMIT)
                .build();
        
        log.info("사용자 티켓 상태 조회 완료 - userId: {}, remainingTickets: {}, dailyLimit: {}", 
                userId, response.getRemainingTickets(), response.getDailyLimit());
        
        return response;
    }
}
