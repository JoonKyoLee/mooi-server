package com.example.emotion_storage.timecapsule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.timecapsule.dto.response.TimeCapsuleExistDateResponse;
import com.example.emotion_storage.timecapsule.repository.TimeCapsuleRepository;
import com.example.emotion_storage.user.repository.UserRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TimeCapsuleServiceTest {

    @InjectMocks private TimeCapsuleService timeCapsuleService;
    @Mock private UserRepository userRepository;
    @Mock private TimeCapsuleRepository timeCapsuleRepository;

    @Test
    void 타임캡슐이_존재하는_날짜_목록을_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        Long userId = 1L;

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Date> repoResult = List.of(
                Date.valueOf(LocalDate.of(year, month, 2)),
                Date.valueOf(LocalDate.of(year, month, 18))
        );

        when(timeCapsuleRepository.findActiveDatesInRange(eq(userId), eq(start), eq(end)))
                .thenReturn(repoResult);

        // when
        ApiResponse<TimeCapsuleExistDateResponse> response = timeCapsuleService.getMonthlyActiveDates(year, month, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message())
                .isEqualTo(SuccessMessage.GET_MONTHLY_TIME_CAPSULE_DATES_SUCCESS.getMessage());

        TimeCapsuleExistDateResponse data = response.data();
        assertThat(data.totalDates()).isEqualTo(2);
        assertThat(data.dates()).containsExactly(
                LocalDate.of(year, month, 2),
                LocalDate.of(year, month, 18)
        );
    }
}
