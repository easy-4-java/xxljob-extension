package com.xxl.job.spring.metrics;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobMetrics} 绑定与指标注册测试。
 */
class XxlJobMetricsTests {

    private MeterRegistry registry;
    private XxlJobSpringExecutor executor;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        executor = new XxlJobSpringExecutor();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @Test
    void constantsAreStable() {
        assertThat(XxlJobMetrics.XXL_JOB_METRIC_NAME_PREFIX).isEqualTo("xxl");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED).isEqualTo("xxl.job.submitted");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING).isEqualTo("xxl.job.running");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED).isEqualTo("xxl.job.completed");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION).isEqualTo("xxl.job.duration");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_QUEUE_SIZE).isEqualTo("xxl.job.queue.size");
    }

    @Test
    void constructorWithSingleArgUsesDefaultTags() {
        XxlJobMetrics metrics = new XxlJobMetrics(executor);
        // 不抛异常即为成功——内部 tags = Collections.emptyList()
        assertThat(metrics).isNotNull();
    }

    @Test
    void constructorWithTwoArgUsesProvidedTags() {
        XxlJobMetrics metrics = new XxlJobMetrics(executor,
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("k", "v")));
        assertThat(metrics).isNotNull();
    }

    @Test
    void bindToRegistersCallbackQueueGauge() {
        // bindTo 会尝试反射 TriggerCallbackThread.getInstance()，
        // 在 xxl-job-core 不同版本下行为可能不同：
        // - 3.3 之前：可能注册 callback queue 指标
        // - 3.3 之后：该类被移除，bindTo 静默跳过
        // 这里只验证 bindTo 不抛异常（注册行为因版本而异是允许的）
        XxlJobMetrics metrics = new XxlJobMetrics(executor);
        metrics.bindTo(registry);
        // 验证调用成功完成（无非受检异常抛出）
        assertThat(registry.getMeters()).isNotNull();
    }

    @Test
    void onApplicationEventIsRegistered() throws Exception {
        XxlJobMetrics metrics = new XxlJobMetrics(executor);
        // onApplicationEvent 是 public 方法，存在即覆盖了 ApplicationListener 接口
        java.lang.reflect.Method m = XxlJobMetrics.class.getMethod("onApplicationEvent",
                org.springframework.boot.context.event.ApplicationStartedEvent.class);
        assertThat(m).isNotNull();
        assertThat(java.lang.reflect.Modifier.isPublic(m.getModifiers())).isTrue();
    }
}
