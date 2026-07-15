package com.xxl.job.spring.metrics;

import com.xxl.job.core.annotation.XxlJobCron;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MetricMethodJobHandler} init / destroy / execute 指标包装测试。
 */
class MetricMethodJobHandlerTests {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @XxlJob("testHandler")
    public static void annotatedMethod() { /* no-op */ }

    public static void plainMethod() { /* no-op */ }

    public static void initMethod() { /* called by init */ }

    public static void destroyMethod() { /* called by destroy */ }

    public static void failingMethod() throws Exception {
        throw new Exception("boom");
    }

    @Test
    void constructorCreatesMetrics() throws Exception {
        Method m = MetricMethodJobHandlerTests.class.getMethod("annotatedMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, m, null, null, java.util.Collections.emptyList());
        assertThat(registry.find("xxl.job.submitted").counter()).isNotNull();
        assertThat(registry.find("xxl.job.running").counter()).isNotNull();
        assertThat(registry.find("xxl.job.completed").counter()).isNotNull();
        assertThat(registry.find("xxl.job.duration").timer()).isNotNull();
    }

    @Test
    void initCallsInitMethod() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        Method init   = MetricMethodJobHandlerTests.class.getMethod("initMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, init, null, java.util.Collections.emptyList());
        handler.init(); // should not throw
    }

    @Test
    void destroyCallsDestroyMethod() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        Method destroy = MetricMethodJobHandlerTests.class.getMethod("destroyMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, destroy, java.util.Collections.emptyList());
        handler.destroy(); // should not throw
    }

    @Test
    void initWithNullInitMethodDoesNothing() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());
        handler.init();
    }

    @Test
    void destroyWithNullDestroyMethodDoesNothing() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());
        handler.destroy();
    }

    @Test
    void executeRecordsMetricsAndIncrementsCounters() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("annotatedMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());

        handler.execute();

        assertThat(registry.find("xxl.job.submitted").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("xxl.job.completed").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("xxl.job.duration").timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    void executeReThrowsExceptionAndStillRecords() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("failingMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());

        assertThatThrownBy(handler::execute)
                .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
                .hasCauseInstanceOf(Exception.class)
                .satisfies(t -> assertThat(t.getCause()).hasMessage("boom"));

        // completed 和 running 都应在 finally 中更新
        assertThat(registry.find("xxl.job.completed").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("xxl.job.running").counter().count()).isEqualTo(0.0); // finally 中 decrement
    }

    @Test
    void executeFallsBackToMethodNameWhenNoAnnotation() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());
        handler.execute();
        assertThat(registry.find("xxl.job.submitted").counter().count()).isEqualTo(1.0);
    }

    @Test
    void toStringIncludesClassAndMethod() throws Exception {
        Method target = MetricMethodJobHandlerTests.class.getMethod("plainMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, target, null, null, java.util.Collections.emptyList());
        assertThat(handler.toString())
                .contains("MetricMethodJobHandler")
                .contains("plainMethod");
    }
}
