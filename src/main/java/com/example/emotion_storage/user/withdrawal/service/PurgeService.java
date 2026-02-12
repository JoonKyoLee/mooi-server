package com.example.emotion_storage.user.withdrawal.service;

import com.example.emotion_storage.user.withdrawal.repository.PurgeCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurgeService {

    private final PurgeCommandRepository purgeCommandRepository;

    @Transactional
    public void purgeUser(Long userId) {
        purgeChatDomain(userId);
        purgeUserProfileDomain(userId);
        purgeTimeCapsuleDomain(userId);
        purgeUserAggregate(userId);
    }

    private void purgeChatDomain(Long userId) {
        purgeCommandRepository.deleteChatByUserId(userId);
    }

    private void purgeUserProfileDomain(Long userId) {
        purgeCommandRepository.deleteNotificationsByUserId(userId);
        purgeCommandRepository.deleteReminderDaysByUserId(userId);
        purgeCommandRepository.deleteExpectationsByUserId(userId);
    }

    private void purgeTimeCapsuleDomain(Long userId) {
        purgeCommandRepository.deleteAnalyzedEmotionByUserId(userId);
        purgeCommandRepository.deleteAnalyzedFeedbackByUserId(userId);
        purgeCommandRepository.deleteEmotionVariationByUserId(userId);
        purgeCommandRepository.deleteKeywordsByUserId(userId);
        purgeCommandRepository.deleteReportsByUserId(userId);
        purgeCommandRepository.deleteTimeCapsulesByUserId(userId);
    }

    private void purgeUserAggregate(Long userId) {
        purgeCommandRepository.deleteChatRoomsByUserId(userId);
        purgeCommandRepository.deleteUserById(userId);
    }
}
