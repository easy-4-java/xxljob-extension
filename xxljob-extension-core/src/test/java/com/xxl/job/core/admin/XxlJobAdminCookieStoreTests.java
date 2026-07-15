package com.xxl.job.core.admin;

import com.xxl.job.core.XxlJobConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobAdminCookieStore} 手工 Cookie 解析测试。
 */
class XxlJobAdminCookieStoreTests {

    @Test
    void parsesRememberMeWithInvalidExpires() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("XXL_JOB_LOGIN_IDENTITY=tok; Path=/; Max-Age=2147483647; "
                + "Expires=Invalid, 19 Jan 2038 03:14:07 GMT");
        assertThat(store.get("XXL_JOB_LOGIN_IDENTITY")).isEqualTo("tok");
    }

    @Test
    void storesMultipleCookiesFromHeaders() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbFromHeaders(Arrays.asList(
                "XXL_JOB_LOGIN_IDENTITY=v2; Path=/",
                "xxl_job_login_token=v3; Path=/; HttpOnly"));

        assertThat(store.buildCookieHeader())
                .contains("XXL_JOB_LOGIN_IDENTITY=v2")
                .contains("xxl_job_login_token=v3");
    }

    @Test
    void absorbsNullOrEmptyHeadersAsNoOp() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbFromHeaders(null);
        store.absorbFromHeaders(Collections.emptyList());
        assertThat(store.isEmpty()).isTrue();
    }

    @Test
    void absorbOneHandlesNullAndEmptyAndMalformedInputs() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne(null);
        store.absorbOne("");
        store.absorbOne("no_equals_sign");
        store.absorbOne("=value_without_name");
        assertThat(store.isEmpty()).isTrue();

        store.absorbOne("name=value");
        assertThat(store.get("name")).isEqualTo("value");
    }

    @Test
    void absorbOneKeepsLastValueForRepeatedName() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("n=1");
        store.absorbOne("n=2");
        assertThat(store.get("n")).isEqualTo("2");
    }

    @Test
    void clearRemovesAllCookies() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("k=v");
        store.clear();
        assertThat(store.isEmpty()).isTrue();
        assertThat(store.buildCookieHeader()).isNull();
    }

    @Test
    void hasLoginCookieReturnsTrueForKnownNameOnly() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("xxl_job_login_token=tok; Path=/");
        assertThat(store.hasLoginCookie("xxl_job_login_token")).isTrue();
        assertThat(store.hasLoginCookie("XXL_JOB_LOGIN_IDENTITY")).isFalse();
        assertThat(store.hasLoginCookie("missing")).isFalse();
        assertThat(store.hasLoginCookie(null)).isFalse();
    }

    @Test
    void buildCookieHeaderReturnsNullWhenEmpty() {
        assertThat(new XxlJobAdminCookieStore().buildCookieHeader()).isNull();
    }

    @Test
    void buildCookieHeaderPreferredNameReturnsOnlyPreferredCookie() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("XXL_JOB_LOGIN_IDENTITY=v2; Path=/");
        store.absorbOne("xxl_job_login_token=v3; Path=/; HttpOnly");

        assertThat(store.buildCookieHeader(XxlJobConstants.COOKIE_LOGIN_TOKEN))
                .isEqualTo("xxl_job_login_token=v3");
        assertThat(store.buildCookieHeader(XxlJobConstants.COOKIE_LOGIN_IDENTITY))
                .isEqualTo("XXL_JOB_LOGIN_IDENTITY=v2");
    }

    @Test
    void buildCookieHeaderReturnsAllJoinedWhenNoPreferenceMatch() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("a=v1");
        store.absorbOne("b=v2");
        assertThat(store.buildCookieHeader("nonexistent"))
                .contains("a=v1").contains("b=v2");
    }

    @Test
    void buildCookieHeaderPreferredNameReturnsAllWhenPreferredNotPresent() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("a=v1");
        store.absorbOne("b=v2");
        // preferredName 为 null 时返回所有 cookie
        assertThat(store.buildCookieHeader(null))
                .contains("a=v1").contains("b=v2");
    }

    @Test
    void absorbOneHandlesNameOnlyCookie() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("theme=dark");
        assertThat(store.get("theme")).isEqualTo("dark");
    }

    @Test
    void absorbOneHandlesMultipleAttributes() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("session=abc123; Path=/; HttpOnly; Secure");
        assertThat(store.get("session")).isEqualTo("abc123");
    }

    @Test
    void absorbOneHandlesEmptyValue() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("token=");
        assertThat(store.get("token")).isEqualTo("");
    }

    @Test
    void absorbOneHandlesWhitespaceAroundNameValue() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("  token  =  abc123  ; Path=/");
        assertThat(store.get("token")).isEqualTo("abc123");
    }

    @Test
    void absorbOneHandlesEqualsSignInValue() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("token=abc=def=ghi; Path=/");
        assertThat(store.get("token")).isEqualTo("abc=def=ghi");
    }

    @Test
    void hasLoginCookieReturnsFalseForEmptyName() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("k=v");
        assertThat(store.hasLoginCookie("")).isFalse();
    }

    @Test
    void isEmptyReturnsTrueWhenNoCookies() {
        assertThat(new XxlJobAdminCookieStore().isEmpty()).isTrue();
    }

    @Test
    void isEmptyReturnsFalseAfterAbsorb() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("k=v");
        assertThat(store.isEmpty()).isFalse();
    }

    @Test
    void getReturnsNullForUnknownName() {
        XxlJobAdminCookieStore store = new XxlJobAdminCookieStore();
        store.absorbOne("k=v");
        assertThat(store.get("missing")).isNull();
    }

    @Test
    void getReturnsNullForEmptyStore() {
        assertThat(new XxlJobAdminCookieStore().get("anything")).isNull();
    }
}
