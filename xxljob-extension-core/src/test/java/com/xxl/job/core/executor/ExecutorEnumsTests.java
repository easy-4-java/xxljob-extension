package com.xxl.job.core.executor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * executor 子包下各枚举的稳定性测试。
 */
class ExecutorEnumsTests {

    @Test
    void executorRouteStrategyEnumValues() {
        assertThat(ExecutorRouteStrategyEnum.values())
                .containsExactly(
                        ExecutorRouteStrategyEnum.FIRST,
                        ExecutorRouteStrategyEnum.LAST,
                        ExecutorRouteStrategyEnum.ROUND,
                        ExecutorRouteStrategyEnum.RANDOM,
                        ExecutorRouteStrategyEnum.CONSISTENT_HASH,
                        ExecutorRouteStrategyEnum.LEAST_FREQUENTLY_USED,
                        ExecutorRouteStrategyEnum.LEAST_RECENTLY_USED,
                        ExecutorRouteStrategyEnum.FAILOVER,
                        ExecutorRouteStrategyEnum.BUSYOVER,
                        ExecutorRouteStrategyEnum.SHARDING_BROADCAST);
    }

    @Test
    void executorRouteStrategyEnumToStringContainsNameAndDesc() {
        assertThat(ExecutorRouteStrategyEnum.FIRST.toString()).isEqualTo("FIRST -> 第一个");
        assertThat(ExecutorRouteStrategyEnum.SHARDING_BROADCAST.toString()).isEqualTo("SHARDING_BROADCAST -> 分片广播");
    }

    @Test
    void executorTriggerPeriodEnumValuesAndToString() {
        assertThat(ExecutorTriggerPeriodEnum.values())
                .containsExactly(ExecutorTriggerPeriodEnum.WEEK, ExecutorTriggerPeriodEnum.MONTH, ExecutorTriggerPeriodEnum.DAILY);
        assertThat(ExecutorTriggerPeriodEnum.WEEK.toString()).isEqualTo("WEEK -> 每周");
        assertThat(ExecutorTriggerPeriodEnum.DAILY.toString()).isEqualTo("DAILY -> 每天");
    }

    @Test
    void misfireStrategyEnumValues() {
        assertThat(MisfireStrategyEnum.values())
                .containsExactly(MisfireStrategyEnum.DO_NOTHING, MisfireStrategyEnum.FIRE_ONCE_NOW);
    }

    @Test
    void misfireStrategyEnumMatchByNameReturnsItem() {
        assertThat(MisfireStrategyEnum.match("DO_NOTHING", MisfireStrategyEnum.FIRE_ONCE_NOW))
                .isEqualTo(MisfireStrategyEnum.DO_NOTHING);
        assertThat(MisfireStrategyEnum.match("FIRE_ONCE_NOW", MisfireStrategyEnum.DO_NOTHING))
                .isEqualTo(MisfireStrategyEnum.FIRE_ONCE_NOW);
    }

    @Test
    void misfireStrategyEnumMatchReturnsDefaultForUnknown() {
        assertThat(MisfireStrategyEnum.match("UNKNOWN", MisfireStrategyEnum.DO_NOTHING))
                .isEqualTo(MisfireStrategyEnum.DO_NOTHING);
    }

    @Test
    void misfireStrategyEnumMatchReturnsDefaultForNull() {
        assertThat(MisfireStrategyEnum.match(null, MisfireStrategyEnum.FIRE_ONCE_NOW))
                .isEqualTo(MisfireStrategyEnum.FIRE_ONCE_NOW);
    }

    @Test
    void scheduleTypeEnumValues() {
        assertThat(ScheduleTypeEnum.values())
                .containsExactly(ScheduleTypeEnum.NONE, ScheduleTypeEnum.CRON, ScheduleTypeEnum.FIX_RATE);
    }

    @Test
    void scheduleTypeEnumGetTitle() {
        assertThat(ScheduleTypeEnum.NONE.getTitle()).isEqualTo("无");
        assertThat(ScheduleTypeEnum.CRON.getTitle()).isEqualTo("CRON");
        assertThat(ScheduleTypeEnum.FIX_RATE.getTitle()).isEqualTo("固定速度");
    }

    @Test
    void scheduleTypeEnumMatchByName() {
        assertThat(ScheduleTypeEnum.match("CRON", ScheduleTypeEnum.NONE)).isEqualTo(ScheduleTypeEnum.CRON);
        assertThat(ScheduleTypeEnum.match("NONE", ScheduleTypeEnum.CRON)).isEqualTo(ScheduleTypeEnum.NONE);
        assertThat(ScheduleTypeEnum.match("UNKNOWN", ScheduleTypeEnum.CRON)).isEqualTo(ScheduleTypeEnum.CRON);
    }
}
