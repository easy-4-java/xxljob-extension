package com.xxl.job.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ReturnT} 通用响应包装测试。
 */
class ReturnTTests {

    @Test
    void defaultConstructorLeavesFieldsUnset() {
        ReturnT<String> r = new ReturnT<>();
        assertThat(r.getCode()).isZero();
        assertThat(r.getMsg()).isNull();
        assertThat(r.getContent()).isNull();
    }

    @Test
    void successConstructorSetsSuccessCodeAndContent() {
        ReturnT<String> r = new ReturnT<>("hello");
        assertThat(r.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(r.getContent()).isEqualTo("hello");
        assertThat(r.getMsg()).isNull();
    }

    @Test
    void failConstructorSetsCodeAndMsg() {
        ReturnT<String> r = new ReturnT<>(ReturnT.FAIL_CODE, "boom");
        assertThat(r.getCode()).isEqualTo(ReturnT.FAIL_CODE);
        assertThat(r.getMsg()).isEqualTo("boom");
        assertThat(r.getContent()).isNull();
    }

    @Test
    void settersRoundTrip() {
        ReturnT<Integer> r = new ReturnT<>();
        r.setCode(201);
        r.setMsg("created");
        r.setContent(42);
        assertThat(r.getCode()).isEqualTo(201);
        assertThat(r.getMsg()).isEqualTo("created");
        assertThat(r.getContent()).isEqualTo(42);
    }

    @Test
    void staticInstances() {
        assertThat(ReturnT.SUCCESS.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(ReturnT.FAIL.getCode()).isEqualTo(ReturnT.FAIL_CODE);
        assertThat(ReturnT.SUCCESS_CODE).isEqualTo(200);
        assertThat(ReturnT.FAIL_CODE).isEqualTo(500);
    }

    @Test
    void toStringIncludesAllFields() {
        ReturnT<String> r = new ReturnT<>("x");
        String s = r.toString();
        assertThat(s)
                .contains("code=" + ReturnT.SUCCESS_CODE)
                .contains("msg=null")
                .contains("content=x");
    }
}
