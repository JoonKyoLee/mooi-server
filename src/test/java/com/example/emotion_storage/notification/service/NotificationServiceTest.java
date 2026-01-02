package com.example.emotion_storage.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.emotion_storage.notification.domain.NotificationType;
import com.example.emotion_storage.notification.repository.NotificationRepository;
import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class NotificationServiceTest {

    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

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
    }

    @Test
    void 타임캡슐_도착_알림이_생성된다() {
        // given
        Long timeCapsuleId = 1L;

        // when
        notificationService.createTimeCapsuleArrival(userId, timeCapsuleId);

        // then
        assertThat(notificationRepository.existsByUser_IdAndTypeAndTargetId(
                userId, NotificationType.TIME_CAPSULE_ARRIVAL, timeCapsuleId
        )).isTrue();
    }

    @Test
    void 같은_타겟이면_알림이_중복_생성되지_않는다() {
        // given
        Long timeCapsuleId = 1L;

        // when
        notificationService.createTimeCapsuleArrival(userId, timeCapsuleId);
        notificationService.createTimeCapsuleArrival(userId, timeCapsuleId);

        // then
        long count = notificationRepository.countByUser_IdAndTypeAndTargetId(
                userId, NotificationType.TIME_CAPSULE_ARRIVAL, timeCapsuleId
        );
        assertThat(count).isEqualTo(1);
    }
}
