package com.xxl.job.core.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * extension 自己的 {@link ExecutorBlockStrategyEnum} 枚举测试。
 */
class ExecutorBlockStrategyEnumTests {

    @Test
    void valuesAreStable() {
        assertThat(ExecutorBlockStrategyEnum.values())
                .containsExactly(
                        ExecutorBlockStrategyEnum.SERIAL_EXECUTION,
                        ExecutorBlockStrategyEnum.DISCARD_LATER,
                        ExecutorBlockStrategyEnum.COVER_EARLY);
    }

    @Test
    void getTitleAndSetter() {
        ExecutorBlockStrategyEnum s = ExecutorBlockStrategyEnum.SERIAL_EXECUTION;
        assertThat(s.getTitle()).isEqualTo("Serial execution");
        s.setTitle("Custom Title");
        assertThat(s.getTitle()).isEqualTo("Custom Title");
    }

    @Test
    void matchByName() {
        assertThat(ExecutorBlockStrategyEnum.match("SERIAL_EXECUTION", ExecutorBlockStrategyEnum.COVER_EARLY))
                .isEqualTo(ExecutorBlockStrategyEnum.SERIAL_EXECUTION);
    }

    @Test
    void matchReturnsDefaultForNull() {
        assertThat(ExecutorBlockStrategyEnum.match(null, ExecutorBlockStrategyEnum.DISCARD_LATER))
                .isEqualTo(ExecutorBlockStrategyEnum.DISCARD_LATER);
    }

    @Test
    void matchReturnsDefaultForUnknown() {
        assertThat(ExecutorBlockStrategyEnum.match("UNKNOWN", ExecutorBlockStrategyEnum.COVER_EARLY))
                .isEqualTo(ExecutorBlockStrategyEnum.COVER_EARLY);
    }
}
