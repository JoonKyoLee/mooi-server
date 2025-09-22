package com.example.emotion_storage.timecapsule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TimeCapsuleTest {

    private TimeCapsule newTimeCapsule() {
        return TimeCapsule.builder()
                .id(1L)
                .user(null)
                .report(null)
                .chatroomId(123L)
                .historyDate(LocalDateTime.now())
                .oneLineSummary("요약")
                .dialogueSummary("대화")
                .myMindNote(null)
                .isOpened(false)
                .isTempSave(false)
                .isFavorite(false)
                .build();
    }

    @Test
    void 타임캡슐이_정상적으로_생성된다() {
        // given & when
        TimeCapsule timeCapsule = newTimeCapsule();

        // then
        assertThat(timeCapsule.getChatroomId()).isEqualTo(123L);
        assertThat(timeCapsule.getHistoryDate()).isNotNull();
        assertThat(timeCapsule.getOneLineSummary()).isEqualTo("요약");
        assertThat(timeCapsule.getDialogueSummary()).isEqualTo("대화");
        assertThat(timeCapsule.getIsOpened()).isFalse();
        assertThat(timeCapsule.getIsTempSave()).isFalse();
        assertThat(timeCapsule.getIsFavorite()).isFalse();
    }

    @Test
    void 타임캡슐_즐겨찾기에_성공한다() {
        // given
        TimeCapsule timeCapsule = newTimeCapsule();
        LocalDateTime before = LocalDateTime.now();

        // when
        timeCapsule.markFavorite();

        // then
        assertThat(timeCapsule.getIsFavorite()).isTrue();
        assertThat(timeCapsule.getFavoriteAt()).isNotNull();
        assertThat(timeCapsule.getFavoriteAt()).isAfterOrEqualTo(before);
    }

    @Test
    void 타임캡슐_즐겨찾기_헤제에_성공한다() {
        // given
        TimeCapsule timeCapsule = newTimeCapsule();

        // when
        timeCapsule.unmarkFavorite();

        // then
        assertThat(timeCapsule.getIsFavorite()).isFalse();
        assertThat(timeCapsule.getFavoriteAt()).isNull();
    }

    @Test
    void 열림_상태로_변경에_성공한다() {
        // given
        TimeCapsule timeCapsule = newTimeCapsule();

        // when
        timeCapsule.setIsOpened(true);

        // then
        assertThat(timeCapsule.getIsOpened()).isTrue();
    }

    @Test
    void 마음노트_업데이트에_성공한다() {
        // given
        TimeCapsule timeCapsule = newTimeCapsule();

        // when
        timeCapsule.updateMyMindNote("마음노트 업데이트");

        // then
        assertThat(timeCapsule.getMyMindNote()).isEqualTo("마음노트 업데이트");
    }

    @Test
    void 타임캡슐_삭제에_성공한다() {
        // given
        TimeCapsule timeCapsule = newTimeCapsule();
        LocalDateTime before = LocalDateTime.now().minusSeconds(30);

        // when
        timeCapsule.setDeletedAt(LocalDateTime.now());

        // then
        assertThat(timeCapsule.getDeletedAt()).isNotNull();
        assertThat(timeCapsule.getDeletedAt()).isAfterOrEqualTo(before);
    }
}
