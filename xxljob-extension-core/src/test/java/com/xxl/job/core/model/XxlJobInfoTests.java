package com.xxl.job.core.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobInfo} 任务信息模型测试。
 */
class XxlJobInfoTests {

    @Test
    void settersAndGettersRoundTrip() {
        XxlJobInfo info = new XxlJobInfo();
        Date now = new Date();
        info.setId(1);
        info.setJobGroup(2);
        info.setJobDesc("desc");
        info.setAuthor("auth");
        info.setAlarmEmail("al@x.com");
        info.setScheduleType("CRON");
        info.setScheduleConf("0 0 3 * * ?");
        info.setJobCron("0 0 3 * * ?");
        info.setMisfireStrategy("DO_NOTHING");
        info.setExecutorRouteStrategy("FIRST");
        info.setExecutorHandler("handler");
        info.setExecutorParam("p");
        info.setExecutorBlockStrategy("SERIAL_EXECUTION");
        info.setExecutorTimeout(60);
        info.setExecutorFailRetryCount(3);
        info.setGlueType("BEAN");
        info.setGlueSource("// java");
        info.setGlueRemark("r");
        info.setGlueUpdatetime(now);
        info.setChildJobId("1,2");
        info.setAddTime(now);
        info.setUpdateTime(now);
        info.setTriggerStatus(1);
        info.setTriggerLastTime(100L);
        info.setTriggerNextTime(200L);
        info.setSelfStarting(true);

        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getJobGroup()).isEqualTo(2);
        assertThat(info.getJobDesc()).isEqualTo("desc");
        assertThat(info.getAuthor()).isEqualTo("auth");
        assertThat(info.getAlarmEmail()).isEqualTo("al@x.com");
        assertThat(info.getScheduleType()).isEqualTo("CRON");
        assertThat(info.getScheduleConf()).isEqualTo("0 0 3 * * ?");
        assertThat(info.getJobCron()).isEqualTo("0 0 3 * * ?");
        assertThat(info.getMisfireStrategy()).isEqualTo("DO_NOTHING");
        assertThat(info.getExecutorRouteStrategy()).isEqualTo("FIRST");
        assertThat(info.getExecutorHandler()).isEqualTo("handler");
        assertThat(info.getExecutorParam()).isEqualTo("p");
        assertThat(info.getExecutorBlockStrategy()).isEqualTo("SERIAL_EXECUTION");
        assertThat(info.getExecutorTimeout()).isEqualTo(60);
        assertThat(info.getExecutorFailRetryCount()).isEqualTo(3);
        assertThat(info.getGlueType()).isEqualTo("BEAN");
        assertThat(info.getGlueSource()).isEqualTo("// java");
        assertThat(info.getGlueRemark()).isEqualTo("r");
        assertThat(info.getGlueUpdatetime()).isSameAs(now);
        assertThat(info.getChildJobId()).isEqualTo("1,2");
        assertThat(info.getAddTime()).isSameAs(now);
        assertThat(info.getUpdateTime()).isSameAs(now);
        assertThat(info.getTriggerStatus()).isEqualTo(1);
        assertThat(info.getTriggerLastTime()).isEqualTo(100L);
        assertThat(info.getTriggerNextTime()).isEqualTo(200L);
        assertThat(info.isSelfStarting()).isTrue();
    }

    @Test
    void defaultsAreZeroFalseNull() {
        XxlJobInfo info = new XxlJobInfo();
        assertThat(info.getId()).isNull();
        assertThat(info.getJobGroup()).isNull();
        assertThat(info.getScheduleConf()).isNull();
        assertThat(info.getExecutorTimeout()).isZero();
        assertThat(info.getExecutorFailRetryCount()).isZero();
        assertThat(info.getTriggerStatus()).isZero();
        assertThat(info.getTriggerLastTime()).isZero();
        assertThat(info.getTriggerNextTime()).isZero();
        assertThat(info.isSelfStarting()).isFalse();
    }

    @Test
    void toStringIncludesClassName() {
        assertThat(new XxlJobInfo().toString()).contains("XxlJobInfo");
    }
}
