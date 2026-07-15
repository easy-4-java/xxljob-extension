package com.xxl.job.core.config;

import com.xxl.job.core.AdminVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobAdminConfig} 轻量配置载体 builder/默认值测试。
 */
class XxlJobAdminConfigTests {

    @Test
    void builderWiresAllFields() {
        XxlJobAdminConfig c = XxlJobAdminConfig.builder()
                .accessToken("tok")
                .addresses("http://x")
                .username("u")
                .password("p")
                .remember(true)
                .version(AdminVersion.V3_X)
                .build();

        assertThat(c.getAccessToken()).isEqualTo("tok");
        assertThat(c.getAddresses()).isEqualTo("http://x");
        assertThat(c.getUsername()).isEqualTo("u");
        assertThat(c.getPassword()).isEqualTo("p");
        assertThat(c.isRemember()).isTrue();
        assertThat(c.getVersion()).isEqualTo(AdminVersion.V3_X);
    }

    @Test
    void defaultsAreRememberFalseAndV2X() {
        XxlJobAdminConfig c = XxlJobAdminConfig.builder()
                .addresses("a").username("u").password("p").build();

        assertThat(c.isRemember()).isFalse();
        assertThat(c.getVersion()).isEqualTo(AdminVersion.V2_X);
        assertThat(c.getAccessToken()).isNull();
    }

    @Test
    void ofFactoryMatchesBuilder() {
        XxlJobAdminConfig c = XxlJobAdminConfig.of("http://x", "u", "p");
        assertThat(c.getAddresses()).isEqualTo("http://x");
        assertThat(c.getUsername()).isEqualTo("u");
        assertThat(c.getPassword()).isEqualTo("p");
        assertThat(c.getVersion()).isEqualTo(AdminVersion.V2_X);
        assertThat(c.isRemember()).isFalse();
    }

    @Test
    void settersAndGettersRoundTrip() {
        XxlJobAdminConfig c = XxlJobAdminConfig.builder().accessToken("t").build();
        c.setAccessToken("t"); // 重复赋值，确保 setter 路径覆盖
        c.setAddresses("a");
        c.setUsername("u");
        c.setPassword("p");
        c.setRemember(true);
        c.setVersion(AdminVersion.V3_2_X);
        assertThat(c.getAccessToken()).isEqualTo("t");
        assertThat(c.isRemember()).isTrue();
        assertThat(c.getVersion()).isEqualTo(AdminVersion.V3_2_X);
    }
}
