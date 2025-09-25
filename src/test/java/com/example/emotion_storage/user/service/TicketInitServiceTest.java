package com.example.emotion_storage.user.service;

import com.example.emotion_storage.user.domain.Gender;
import com.example.emotion_storage.user.domain.SocialType;
import com.example.emotion_storage.user.domain.User;
import com.example.emotion_storage.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketInitServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketInitService ticketResetService;

    @Test
    void 활성_사용자들의_티켓_수를_초기화한다() {
        // given
        User user1 = createUser(1L, 5L);
        User user2 = createUser(2L, 3L);
        User user3 = createUser(3L, 0L);
        List<User> activeUsers = Arrays.asList(user1, user2, user3);

        when(userRepository.findAllActiveUsers()).thenReturn(activeUsers);
        when(userRepository.saveAll(any())).thenReturn(activeUsers);

        // when
        ticketResetService.initAllUserTickets();

        // then
        verify(userRepository).findAllActiveUsers();
        verify(userRepository).saveAll(activeUsers);
        
        // 모든 사용자의 티켓 수가 10으로 초기화되었는지 확인
        assert user1.getTicketCount() == 10L;
        assert user2.getTicketCount() == 10L;
        assert user3.getTicketCount() == 10L;
    }

    @Test
    void 활성_사용자가_없을_때도_정상_동작한다() {
        // given
        when(userRepository.findAllActiveUsers()).thenReturn(Arrays.asList());
        when(userRepository.saveAll(any())).thenReturn(Arrays.asList());

        // when
        ticketResetService.initAllUserTickets();

        // then
        verify(userRepository).findAllActiveUsers();
        verify(userRepository).saveAll(Arrays.asList());
    }

    private User createUser(Long id, Long ticketCount) {
        return User.builder()
                .id(id)
                .socialType(SocialType.KAKAO)
                .socialId("test_social_id_" + id)
                .email("test" + id + "@example.com")
                .nickname("test_user_" + id)
                .gender(Gender.MALE)
                .birthday(LocalDate.of(1990, 1, 1))
                .keyCount(100L)
                .ticketCount(ticketCount)
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isMarketingAgreed(false)
                .build();
    }
}
