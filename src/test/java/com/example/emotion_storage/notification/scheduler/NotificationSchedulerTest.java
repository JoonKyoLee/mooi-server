package com.example.emotion_storage.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.emotion_storage.notification.domain.NotificationType;
import com.example.emotion_storage.notification.repository.NotificationRepository;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class NotificationSchedulerTest {

    @Autowired private NotificationScheduler notificationScheduler;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TimeCapsuleRepository timeCapsuleRepository;

    private Long userId;

    @BeforeEach
    void setup() {
        User user = userRepository.save(
                User.builder()
                        .socialType(SocialType.GOOGLE)
                        .socialId("social123")
                        .email("test@example.com")
                        .profileImageUrl("http://example.com/profile.png")
                        .nickname("tester")
                        .gender(Gender.MALE)
                        .birthday(LocalDate.of(2000, 1, 1))
                        .keyCount(5L)
                        .ticketCount(10L)
                        .isTermsAgreed(true)
                        .isPrivacyAgreed(true)
                        .isMarketingAgreed(false)
                        .build()
        );
        userId = user.getId();

        // 생성 대상
        timeCapsuleRepository.save(TimeCapsule.builder()
                .user(user)
                .chatroomId(101L)
                .historyDate(LocalDateTime.now().minusDays(1))
                .oneLineSummary("A")
                .dialogueSummary("A")
                .myMindNote("")
                .isOpened(false)
                .isTempSave(false)
                .isFavorite(false)
                .openedAt(LocalDateTime.now().minusMinutes(1))
                .build());

        // openedAt 미래라 대상 아님
        timeCapsuleRepository.save(TimeCapsule.builder()
                .user(user)
                .chatroomId(102L)
                .historyDate(LocalDateTime.now().minusDays(1))
                .oneLineSummary("B")
                .dialogueSummary("B")
                .myMindNote("")
                .isOpened(false)
                .isTempSave(false)
                .isFavorite(false)
                .openedAt(LocalDateTime.now().plusMinutes(10))
                .build());

        // tempSave라 대상 아님
        timeCapsuleRepository.save(TimeCapsule.builder()
                .user(user)
                .chatroomId(103L)
                .historyDate(LocalDateTime.now().minusDays(1))
                .oneLineSummary("C")
                .dialogueSummary("C")
                .myMindNote("")
                .isOpened(false)
                .isTempSave(true)
                .isFavorite(false)
                .openedAt(null)
                .build());
    }

    @Test
    void 스케줄러가_대상_타임캡슐에만_알림을_생성한다() {
        // when
        notificationScheduler.createTimeCapsuleArrivalNotifications();

        // then: A 하나만 생성
        long count = notificationRepository.countByUser_IdAndType(userId, NotificationType.TIME_CAPSULE_ARRIVAL);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 스케줄러를_두번_돌려도_중복_생성되지_않는다() {
        // when
        notificationScheduler.createTimeCapsuleArrivalNotifications();
        notificationScheduler.createTimeCapsuleArrivalNotifications();

        // then
        long count = notificationRepository.countByUser_IdAndType(userId, NotificationType.TIME_CAPSULE_ARRIVAL);
        assertThat(count).isEqualTo(1);
    }
}
