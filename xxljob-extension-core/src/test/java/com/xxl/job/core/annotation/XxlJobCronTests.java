package com.xxl.job.core.annotation;

import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.ExecutorRouteStrategyEnum;
import com.xxl.job.core.executor.MisfireStrategyEnum;
import com.xxl.job.core.executor.ScheduleTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobCron} 注解默认值 + Target/Retention 元注解测试。
 */
class XxlJobCronTests {

    @XxlJobCron
    public void annotated() {
    }

    @XxlJobCron(value = "v", init = "i", destroy = "d", uid = "u",
            cron = "0 0 3 * * ?", author = "op", alarmEmail = "al@x.com",
            scheduleType = ScheduleTypeEnum.FIX_RATE, desc = "desc",
            param = "p", routeStrategy = ExecutorRouteStrategyEnum.FIRST,
            blockStrategy = ExecutorBlockStrategyEnum.DISCARD_LATER,
            misfireStrategy = MisfireStrategyEnum.FIRE_ONCE_NOW,
            timeout = 5, failRetryCount = 2, selfStarting = true)
    public void fullyAnnotated() {
    }

    @Test
    void defaultsWhenOnlyAnnotated() throws NoSuchMethodException {
        XxlJobCron a = XxlJobCronTests.class.getMethod("annotated").getAnnotation(XxlJobCron.class);
        assertThat(a.value()).isEmpty();
        assertThat(a.init()).isEmpty();
        assertThat(a.destroy()).isEmpty();
        assertThat(a.uid()).isEmpty();
        assertThat(a.cron()).isEmpty();
        assertThat(a.author()).isEqualTo("xxl-job");
        assertThat(a.alarmEmail()).isEmpty();
        assertThat(a.scheduleType()).isEqualTo(ScheduleTypeEnum.CRON);
        assertThat(a.desc()).isEmpty();
        assertThat(a.param()).isEmpty();
        assertThat(a.routeStrategy()).isEqualTo(ExecutorRouteStrategyEnum.LEAST_FREQUENTLY_USED);
        assertThat(a.blockStrategy()).isEqualTo(ExecutorBlockStrategyEnum.COVER_EARLY);
        assertThat(a.misfireStrategy()).isEqualTo(MisfireStrategyEnum.DO_NOTHING);
        assertThat(a.timeout()).isEqualTo(3000);
        assertThat(a.failRetryCount()).isEqualTo(3);
        assertThat(a.selfStarting()).isFalse();
    }

    @Test
    void allAttributesRoundTrip() throws NoSuchMethodException {
        XxlJobCron a = XxlJobCronTests.class.getMethod("fullyAnnotated").getAnnotation(XxlJobCron.class);
        assertThat(a.value()).isEqualTo("v");
        assertThat(a.init()).isEqualTo("i");
        assertThat(a.destroy()).isEqualTo("d");
        assertThat(a.uid()).isEqualTo("u");
        assertThat(a.cron()).isEqualTo("0 0 3 * * ?");
        assertThat(a.author()).isEqualTo("op");
        assertThat(a.alarmEmail()).isEqualTo("al@x.com");
        assertThat(a.scheduleType()).isEqualTo(ScheduleTypeEnum.FIX_RATE);
        assertThat(a.desc()).isEqualTo("desc");
        assertThat(a.param()).isEqualTo("p");
        assertThat(a.routeStrategy()).isEqualTo(ExecutorRouteStrategyEnum.FIRST);
        assertThat(a.blockStrategy()).isEqualTo(ExecutorBlockStrategyEnum.DISCARD_LATER);
        assertThat(a.misfireStrategy()).isEqualTo(MisfireStrategyEnum.FIRE_ONCE_NOW);
        assertThat(a.timeout()).isEqualTo(5);
        assertThat(a.failRetryCount()).isEqualTo(2);
        assertThat(a.selfStarting()).isTrue();
    }

    @Test
    void annotationTargetsMethodAndType() {
        Target target = XxlJobCron.class.getAnnotation(Target.class);
        assertThat(Arrays.stream(target.value()))
                .contains(ElementType.TYPE, ElementType.METHOD);
    }

    @Test
    void annotationRetentionIsRuntime() {
        Retention r = XxlJobCron.class.getAnnotation(Retention.class);
        assertThat(r.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void annotationIsInherited() {
        assertThat(XxlJobCron.class.isAnnotationPresent(Inherited.class)).isTrue();
    }

    @Test
    void annotationIsPresentOnAnnotatedMethod() throws NoSuchMethodException {
        Method m = XxlJobCronTests.class.getMethod("annotated");
        boolean present = m.isAnnotationPresent(XxlJobCron.class);
        assertThat(present).isTrue();
        // annotationType 返回 null 当类型不是 Annotation
        assertThat(m.getAnnotation(XxlJobCron.class).annotationType())
                .isEqualTo((Class<? extends Annotation>) XxlJobCron.class);
    }
}
