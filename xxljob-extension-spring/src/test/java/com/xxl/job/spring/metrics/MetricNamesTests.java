package com.xxl.job.spring.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MetricNames} 纯字符串拼接测试。
 */
class MetricNamesTests {

    @Test
    void nameSinglePart() {
        assertThat(MetricNames.name("a")).isEqualTo("a");
    }

    @Test
    void nameMultipleParts() {
        assertThat(MetricNames.name("a", "b", "c")).isEqualTo("a.b.c");
    }

    @Test
    void nameElidesNullParts() {
        assertThat(MetricNames.name("a", null, "c")).isEqualTo("a.c");
    }

    @Test
    void nameElidesEmptyParts() {
        assertThat(MetricNames.name("a", "", "c")).isEqualTo("a.c");
    }

    @Test
    void nameClassVariant() {
        String result = MetricNames.name(String.class, "foo", "bar");
        assertThat(result).startsWith("java.lang.String");
        assertThat(result).contains(".foo.bar");
    }

    @Test
    void nameAllNullReturnsEmpty() {
        assertThat(MetricNames.name((String) null)).isEmpty();
    }
}
