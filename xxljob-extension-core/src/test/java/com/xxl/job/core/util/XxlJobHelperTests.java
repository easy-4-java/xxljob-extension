package com.xxl.job.core.util;

import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.ExecutorRouteStrategyEnum;
import com.xxl.job.core.executor.ExecutorTriggerPeriodEnum;
import com.xxl.job.core.executor.ScheduleTypeEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.model.XxlJobInfo;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link XxlJobHelper} 工具类测试（构建任务 / Cron 表达式 / 时间格式化）。
 */
class XxlJobHelperTests {

    @Test
    void buildJobInfoPopulatesAllFields() {
        XxlJobInfo info = XxlJobHelper.buildJobInfo(7,
                ScheduleTypeEnum.CRON,
                "0 0 3 * * ?",
                "desc",
                "author",
                "handler",
                GlueTypeEnum.BEAN,
                ExecutorRouteStrategyEnum.FIRST,
                ExecutorBlockStrategyEnum.COVER_EARLY,
                "callback");

        assertThat(info.getJobGroup()).isEqualTo(7);
        assertThat(info.getScheduleType()).isEqualTo("CRON");
        assertThat(info.getScheduleConf()).isEqualTo("0 0 3 * * ?");
        assertThat(info.getJobCron()).isEqualTo("0 0 3 * * ?");
        assertThat(info.getJobDesc()).isEqualTo("desc");
        assertThat(info.getAuthor()).isEqualTo("author");
        assertThat(info.getExecutorHandler()).isEqualTo("handler");
        assertThat(info.getGlueType()).isEqualTo("BEAN");
        assertThat(info.getExecutorRouteStrategy()).isEqualTo("FIRST");
        assertThat(info.getExecutorBlockStrategy()).isEqualTo("COVER_EARLY");
        assertThat(info.getExecutorParam()).isEqualTo("callback");
    }

    @Test
    void getCronExpressionDailyBuildsAtSecondMinuteHour() throws Exception {
        // 用 06:15:30 保证 hour 字段双位，避免源 'HH:mm:ss'.format 不填充导致的字符串差异
        Date base = new SimpleDateFormat("HH:mm:ss").parse("06:15:30");
        String cron = XxlJobHelper.getCronExpression(null, ExecutorTriggerPeriodEnum.DAILY, base);
        assertThat(cron).isEqualTo("30 15 06 * * ?");
    }

    @Test
    void getCronExpressionMonthlyBuildsWithDay() throws Exception {
        // 12:00:00 中 minute/second 为 "00"——无需填充，保持断言稳定
        Date base = new SimpleDateFormat("HH:mm:ss").parse("12:00:00");
        String cron = XxlJobHelper.getCronExpression(15, ExecutorTriggerPeriodEnum.MONTH, base);
        assertThat(cron).isEqualTo("00 00 12 15 * ?");
    }

    @Test
    void getCronExpressionWeeklyBuildsQuestionMarkDay() throws Exception {
        Date base = new SimpleDateFormat("HH:mm:ss").parse("06:15:30");
        String cron = XxlJobHelper.getCronExpression(2, ExecutorTriggerPeriodEnum.WEEK, base);
        assertThat(cron).isEqualTo("30 15 06 ? * 2");
    }

    @Test
    void getTriggerTimeCronStrReturnsHHmmssFormat() throws Exception {
        Date base = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-07-15 09:08:07");
        assertThat(XxlJobHelper.getTriggerTimeCronStr(base)).isEqualTo("09:08:07");
    }

    @Test
    void getTriggerTimeCronStrThrowsForNull() {
        assertThatThrownBy(() -> XxlJobHelper.getTriggerTimeCronStr(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("triggerTime");
    }
}
