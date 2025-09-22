package com.example.emotion_storage.timecapsule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TimeCapsuleStatusTest {

    @Test
    void 임시_저장_상태의_타임캡슐일_경우에_임시_저장_상태를_반환한다() {
        // given
        TimeCapsule timeCapsule = TimeCapsule.builder()
                .user(null)
                .report(null)
                .chatroomId(123L)
                .historyDate(LocalDateTime.now())
                .oneLineSummary("요약")
                .dialogueSummary("대화")
                .myMindNote(null)
                .isOpened(false)
                .isTempSave(true)
                .isFavorite(false)
                .build();

        // when
        TimeCapsuleStatus timeCapsuleStatus = TimeCapsuleStatus.getStatus(timeCapsule);

        // then
        assertThat(timeCapsuleStatus).isEqualTo(TimeCapsuleStatus.TEMP_SAVED);
        assertThat(timeCapsuleStatus.getStatusMessage()).isEqualTo(TimeCapsuleStatus.TEMP_SAVED.getStatusMessage());
    }

    @Test
    void 잠김_상태의_타임캡슐일_경우에_잠김_상태를_반환한다() {
        // given
        TimeCapsule timeCapsule = TimeCapsule.builder()
                .user(null)
                .report(null)
                .chatroomId(123L)
                .historyDate(LocalDateTime.now())
                .openedAt(LocalDateTime.now().plusDays(3))
                .oneLineSummary("요약")
                .dialogueSummary("대화")
                .myMindNote(null)
                .isOpened(false)
                .isTempSave(false)
                .isFavorite(false)
                .build();

        // when
        TimeCapsuleStatus timeCapsuleStatus = TimeCapsuleStatus.getStatus(timeCapsule);

        // then
        assertThat(timeCapsuleStatus).isEqualTo(TimeCapsuleStatus.LOCKED);
        assertThat(timeCapsuleStatus.getStatusMessage()).isEqualTo(TimeCapsuleStatus.LOCKED.getStatusMessage());
    }

    @Test
    void 열림_상태의_타임캡슐일_경우에_열림_상태를_반환한다() {
        // given
        TimeCapsule timeCapsule = TimeCapsule.builder()
                .user(null)
                .report(null)
                .chatroomId(123L)
                .historyDate(LocalDateTime.now().minusDays(7))
                .openedAt(LocalDateTime.now().minusDays(1))
                .oneLineSummary("요약")
                .dialogueSummary("대화")
                .myMindNote(null)
                .isOpened(true)
                .isTempSave(false)
                .isFavorite(false)
                .build();

        // when
        TimeCapsuleStatus timeCapsuleStatus = TimeCapsuleStatus.getStatus(timeCapsule);

        // then
        assertThat(timeCapsuleStatus).isEqualTo(TimeCapsuleStatus.OPENED);
        assertThat(timeCapsuleStatus.getStatusMessage()).isEqualTo(TimeCapsuleStatus.OPENED.getStatusMessage());
    }

    @Test
    void 도착_상태의_타임캡슐일_경우에_도착_상태를_반환한다() {
        // given
        TimeCapsule timeCapsule = TimeCapsule.builder()
                .user(null)
                .report(null)
                .chatroomId(123L)
                .historyDate(LocalDateTime.now().minusDays(7))
                .openedAt(LocalDateTime.now().minusDays(1))
                .oneLineSummary("요약")
                .dialogueSummary("대화")
                .myMindNote(null)
                .isOpened(false)
                .isTempSave(false)
                .isFavorite(false)
                .build();

        // when
        TimeCapsuleStatus timeCapsuleStatus = TimeCapsuleStatus.getStatus(timeCapsule);

        // then
        assertThat(timeCapsuleStatus).isEqualTo(TimeCapsuleStatus.ARRIVED);
        assertThat(timeCapsuleStatus.getStatusMessage()).isEqualTo(TimeCapsuleStatus.ARRIVED.getStatusMessage());
    }
}
