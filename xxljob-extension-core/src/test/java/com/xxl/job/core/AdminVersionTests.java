package com.xxl.job.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AdminVersion} 枚举的所有分支与语义测试。
 */
class AdminVersionTests {

    @Test
    void valuesCoversAllThreeVersions() {
        assertThat(AdminVersion.values())
                .containsExactly(AdminVersion.V2_X, AdminVersion.V3_2_X, AdminVersion.V3_X);
    }

    @Test
    void valueOfResolvesByName() {
        assertThat(AdminVersion.valueOf("V2_X")).isEqualTo(AdminVersion.V2_X);
        assertThat(AdminVersion.valueOf("V3_2_X")).isEqualTo(AdminVersion.V3_2_X);
        assertThat(AdminVersion.valueOf("V3_X")).isEqualTo(AdminVersion.V3_X);
    }

    @Test
    void usesV3LoginMatches3_2And3_x() {
        assertThat(AdminVersion.V2_X.usesV3Login()).isFalse();
        assertThat(AdminVersion.V3_2_X.usesV3Login()).isTrue();
        assertThat(AdminVersion.V3_X.usesV3Login()).isTrue();
    }

    @Test
    void usesV3FullApiMatchesOnly3_X() {
        assertThat(AdminVersion.V2_X.usesV3FullApi()).isFalse();
        assertThat(AdminVersion.V3_2_X.usesV3FullApi()).isFalse();
        assertThat(AdminVersion.V3_X.usesV3FullApi()).isTrue();
    }

    @Test
    void loginCookieNameReturnsIdentityForV2AndLoginTokenForV3Family() {
        assertThat(AdminVersion.V2_X.loginCookieName())
                .isEqualTo(XxlJobConstants.COOKIE_LOGIN_IDENTITY);
        assertThat(AdminVersion.V3_2_X.loginCookieName())
                .isEqualTo(XxlJobConstants.COOKIE_LOGIN_TOKEN);
        assertThat(AdminVersion.V3_X.loginCookieName())
                .isEqualTo(XxlJobConstants.COOKIE_LOGIN_TOKEN);
    }

    @Test
    void toStringReturnsEnumName() {
        assertThat(AdminVersion.V2_X.toString()).isEqualTo("V2_X");
        assertThat(AdminVersion.V3_2_X.toString()).isEqualTo("V3_2_X");
        assertThat(AdminVersion.V3_X.toString()).isEqualTo("V3_X");
    }
}
