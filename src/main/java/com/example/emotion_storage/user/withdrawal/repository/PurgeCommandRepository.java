package com.example.emotion_storage.user.withdrawal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class PurgeCommandRepository {

    @PersistenceContext
    private EntityManager em;

    public int deleteChatByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE c 
            FROM chat 
            JOIN chatrooms cr ON c.chatroom_id = cr.chatroom_id 
            WHERE cr.user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteAnalyzedEmotionByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE ae
            FROM analyzed_emotion ae
            JOIN time_capsules tc ON ae.time_capsule_id = tc.time_capsule_id
            WHERE tc.user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteAnalyzedFeedbackByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE af
            FROM analyzed_feedback af
            JOIN time_capsules tc ON af.time_capsule_id = tc.time_capsule_id
            WHERE tc.user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteEmotionVariationByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE ev
            FROM emotion_variation ev
            JOIN time_capsules tc ON ev.report_id = tc.report_id
            WHERE tc.user_id = :userId
              AND tc.report_id IS NOT NULL
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteKeywordsByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE k
            FROM keywords k
            JOIN time_capsules tc ON k.report_id = tc.report_id
            WHERE tc.user_id = :userId
              AND tc.report_id IS NOT NULL
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteTimeCapsulesByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM time_capsules
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteReportsByUserId(Long userId) {
        // reports는 time_capsules.report_id로만 연결됨
        return em.createNativeQuery("""
            DELETE r
            FROM reports r
            JOIN time_capsules tc ON r.report_id = tc.report_id
            WHERE tc.user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteNotificationsByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM notifications
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteReminderDaysByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM user_emotion_reminder_days
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteExpectationsByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM user_expectations
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteChatRoomsByUserId(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM chatrooms
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }

    public int deleteUserById(Long userId) {
        return em.createNativeQuery("""
            DELETE FROM users
            WHERE user_id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }
}
