package com.xxl.job.core.admin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobAdminHttpResponse} 不可变响应的 getter / boolean 测试。
 */
class XxlJobAdminHttpResponseTests {

    @Test
    void gettersReturnConstructorValues() {
        XxlJobAdminHttpResponse r = new XxlJobAdminHttpResponse(200, "OK", "{\"a\":1}", "application/json");
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(r.getStatusText()).isEqualTo("OK");
        assertThat(r.getBody()).isEqualTo("{\"a\":1}");
        assertThat(r.getContentType()).isEqualTo("application/json");
    }

    @Test
    void isSuccessRangeInclusive() {
        assertThat(new XxlJobAdminHttpResponse(199, "", "", null).isSuccess()).isFalse();
        assertThat(new XxlJobAdminHttpResponse(200, "", "", null).isSuccess()).isTrue();
        assertThat(new XxlJobAdminHttpResponse(299, "", "", null).isSuccess()).isTrue();
        assertThat(new XxlJobAdminHttpResponse(300, "", "", null).isSuccess()).isFalse();
        assertThat(new XxlJobAdminHttpResponse(500, "", "", null).isSuccess()).isFalse();
    }

    @Test
    void isJsonDetectsContentTypeHeader() {
        assertThat(new XxlJobAdminHttpResponse(200, "", "", "application/json;charset=UTF-8").isJson()).isTrue();
        assertThat(new XxlJobAdminHttpResponse(200, "", "", "application/json").isJson()).isTrue();
        assertThat(new XxlJobAdminHttpResponse(200, "", "", "text/html").isJson()).isFalse();
    }

    @Test
    void isJsonHandlesNullAndEmptyContentType() {
        assertThat(new XxlJobAdminHttpResponse(200, "", "", null).isJson()).isFalse();
        assertThat(new XxlJobAdminHttpResponse(200, "", "", "").isJson()).isFalse();
    }

    @Test
    void errorMessageFromStatusTextWhenPresent() {
        XxlJobAdminHttpResponse r = new XxlJobAdminHttpResponse(500, "Internal Server Error", "{}", "json");
        assertThat(r.errorMessage()).isEqualTo("HTTP 500 Internal Server Error");
    }

    @Test
    void errorMessageFromBodyWhenNoStatusText() {
        XxlJobAdminHttpResponse r = new XxlJobAdminHttpResponse(500, "", "some body text", "json");
        assertThat(r.errorMessage()).startsWith("HTTP 500, body:");
    }

    @Test
    void errorMessageFallsBackToStatusOnlyWhenBothEmpty() {
        XxlJobAdminHttpResponse r = new XxlJobAdminHttpResponse(500, "", "", null);
        assertThat(r.errorMessage()).isEqualTo("HTTP 500");
    }

    @Test
    void safeBodyCapsAtMaxLen() {
        XxlJobAdminHttpResponse r = new XxlJobAdminHttpResponse(200, "", "1234567890", null);
        assertThat(r.safeBody(3)).isEqualTo("123");
        assertThat(r.safeBody(100)).isEqualTo("1234567890");
    }

    @Test
    void safeBodyReportsNull() {
        assertThat(new XxlJobAdminHttpResponse(200, "", null, null).safeBody(10)).isEqualTo("null");
    }
}
