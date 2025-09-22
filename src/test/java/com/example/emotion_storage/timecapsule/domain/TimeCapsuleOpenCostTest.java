package com.example.emotion_storage.timecapsule.domain;

import static com.example.emotion_storage.timecapsule.domain.TimeCapsuleOpenCost.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class TimeCapsuleOpenCostTest {

    @Test
    void 타임캡슐_도착이_1일과_7일_사이라면_하나의_키가_필요하다() {
        // given
        List<Integer> days = IntStream.rangeClosed(ONE_KEY.getMinDays(), ONE_KEY.getMaxDays())
                .boxed()
                .toList();

        // when
        List<Integer> requiredKeys = days.stream()
                .map(TimeCapsuleOpenCost::getRequiredKeys)
                .toList();

        // then
        assertThat(requiredKeys)
                .hasSize(days.size())
                .allMatch(keys -> keys == ONE_KEY.getRequiredKeys());
    }

    @Test
    void 타임캡슐_도착이_8일과_30일_사이라면_3개의_키가_필요하다() {
        // given
        List<Integer> days = IntStream.rangeClosed(THREE_KEYS.getMinDays(), THREE_KEYS.getMaxDays())
                .boxed()
                .toList();

        // when
        List<Integer> requiredKeys = days.stream()
                .map(TimeCapsuleOpenCost::getRequiredKeys)
                .toList();

        // then
        assertThat(requiredKeys)
                .hasSize(days.size())
                .allMatch(keys -> keys == THREE_KEYS.getRequiredKeys());
    }

    @Test
    void 타임캡슐_도착이_31일과_90일_사이라면_7개의_키가_필요하다() {
        // given
        List<Integer> days = IntStream.rangeClosed(SEVEN_KEYS.getMinDays(), SEVEN_KEYS.getMaxDays())
                .boxed()
                .toList();

        // when
        List<Integer> requiredKeys = days.stream()
                .map(TimeCapsuleOpenCost::getRequiredKeys)
                .toList();

        // then
        assertThat(requiredKeys)
                .hasSize(days.size())
                .allMatch(keys -> keys == SEVEN_KEYS.getRequiredKeys());
    }

    @Test
    void 타임캡슐_도착이_91일과_180일_사이라면_11개의_키가_필요하다() {
        // given
        List<Integer> days = IntStream.rangeClosed(ELEVEN_KEYS.getMinDays(), ELEVEN_KEYS.getMaxDays())
                .boxed()
                .toList();

        // when
        List<Integer> requiredKeys = days.stream()
                .map(TimeCapsuleOpenCost::getRequiredKeys)
                .toList();

        // then
        assertThat(requiredKeys)
                .hasSize(days.size())
                .allMatch(keys -> keys == ELEVEN_KEYS.getRequiredKeys());
    }

    @Test
    void 타임캡슐_도착이_181일_이후라면_15개의_키가_필요하다() {
        // given
        int day = 181;

        // when
        int requiredKey = TimeCapsuleOpenCost.getRequiredKeys(day);

        // then
        assertThat(requiredKey).isEqualTo(FIFTEEN_KEYS.getRequiredKeys());
    }
}
