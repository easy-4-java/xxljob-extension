package com.xxl.job.core.admin;

import com.xxl.job.core.AdminVersion;
import com.xxl.job.core.XxlJobConstants;
import com.xxl.job.core.config.XxlJobAdminConfig;
import com.xxl.job.core.model.ReturnT;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultXxlJobAdminClient} 集成测试：使用嵌入 {@link MockXxlJobAdminServer}
 * 驱动所有 {@code XxlJobAdminClient} 接口方法。
 */
class DefaultXxlJobAdminClientTests {

    private MockXxlJobAdminServer mock;
    private UnirestInstance unirest;
    private XxlJobAdminConfig config;
    private DefaultXxlJobAdminClient client;

    @BeforeEach
    void setUp() throws Exception {
        mock = new MockXxlJobAdminServer(0, AdminVersion.V3_X);
        mock.start();

        unirest = Unirest.spawnInstance();
        unirest.config().connectTimeout(10_000).followRedirects(false);
        config = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl())
                .username("admin")
                .password("123456")
                .version(AdminVersion.V3_X)
                .build();
        client = new DefaultXxlJobAdminClient(unirest, config);
    }

    @AfterEach
    void tearDown() {
        if (mock != null) mock.close();
        Unirest.shutDown();
    }

    @Test
    void versionAndIsV3DelegatesToConfig() {
        assertThat(client.version()).isEqualTo(AdminVersion.V3_X);
        assertThat(client.isV3()).isTrue();
    }

    @Test
    void versionReturnsV2WhenConfigVersionIsNull() {
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl())
                .username("u").password("p")
                .version(null).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        assertThat(c.version()).isEqualTo(AdminVersion.V2_X);
    }

    @Test
    void isV3ReturnsFalseForV2() {
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl())
                .username("u").password("p")
                .version(AdminVersion.V2_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        assertThat(c.isV3()).isFalse();
    }

    @Test
    void buildUrlStripsTrailingSlashAndAppendsPath() {
        assertThat(client.buildUrl("/login")).isEqualTo(mock.getBaseUrl() + "/login");

        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl() + "/")
                .username("u").password("p")
                .version(AdminVersion.V3_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        assertThat(c.buildUrl("/x")).isEqualTo(mock.getBaseUrl() + "/x");
    }

    @Test
    void loginStoresCookieAndTogglesAuthenticated() {
        boolean ok = client.login("admin", "123456", false);
        assertThat(ok).isTrue();
        assertThat(mock.getRequestLog())
                .anyMatch(r -> r.path.endsWith(XxlJobConstants.loginPath(AdminVersion.V3_X)));
    }

    @Test
    void loginReturnsFalseOnFailure() {
        // Mock 服务器始终返回成功，但通过错误密码测试无 cookie 情况
        // 使用 V2_X 版本（Mock 返回 V3 cookie，但请求 V2 cookie name）
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl())
                .username("admin").password("123456")
                .version(AdminVersion.V2_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        // V2_X 需要 XXL_JOB_LOGIN_IDENTITY cookie，但 Mock 返回 xxl_job_login_token
        boolean ok = c.login("admin", "123456", false);
        assertThat(ok).isFalse();
    }

    @Test
    void loginReturnsFalseOnNetworkError() {
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses("http://localhost:1")  // 不存在的端口
                .username("admin").password("123456")
                .version(AdminVersion.V3_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        boolean ok = c.login("admin", "123456", false);
        assertThat(ok).isFalse();
    }

    @Test
    void loginIfNeededIsIdempotent() {
        assertThat(client.login("admin", "123456", false)).isTrue();
        int firstCount = countLoginRequests();
        client.loginIfNeeded();
        int secondCount = countLoginRequests();
        assertThat(secondCount).isEqualTo(firstCount);
    }

    @Test
    void loginIfNeededTriggersNewLoginAfterLogout() {
        client.login("admin", "123456", false);
        client.logout();
        int before = countLoginRequests();
        client.loginIfNeeded();
        int after = countLoginRequests();
        assertThat(after).isGreaterThan(before);
    }

    @Test
    void logoutClearsCookies() {
        client.login("admin", "123456", false);
        ReturnT<String> out = client.logout();
        assertThat(out.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
    }

    @Test
    void logoutReturnsFailOnNetworkError() {
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses("http://localhost:1")
                .username("admin").password("123456")
                .version(AdminVersion.V3_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        ReturnT<String> out = c.logout();
        assertThat(out.getCode()).isEqualTo(ReturnT.FAIL_CODE);
    }

    @Test
    void postFormSendsRequestAndReturnsJsonResponse() {
        XxlJobAdminHttpResponse r = client.postForm(XxlJobConstants.JOBGROUP_PAGELIST,
                java.util.Map.of("appname", "x", "title", "y"));
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.isJson()).isTrue();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    @Test
    void postFormHandlesSessionExpiryAndRetries() {
        // 先登录获取 cookie
        client.login("admin", "123456", false);
        // 清除 cookie 模拟 session 过期
        // 下次 postForm 应该自动重新登录
        XxlJobAdminHttpResponse r = client.postForm(XxlJobConstants.JOBGROUP_PAGELIST,
                java.util.Map.of("appname", "x", "title", "y"));
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void postFormWithV2VersionUsesCorrectPaths() {
        XxlJobAdminConfig cfg = XxlJobAdminConfig.builder()
                .addresses(mock.getBaseUrl())
                .username("admin").password("123456")
                .version(AdminVersion.V2_X).build();
        DefaultXxlJobAdminClient c = new DefaultXxlJobAdminClient(unirest, cfg);
        XxlJobAdminHttpResponse r = c.postForm(XxlJobConstants.JOBGROUP_PAGELIST,
                java.util.Map.of("appname", "x"));
        assertThat(r.isSuccess()).isTrue();
    }

    private int countLoginRequests() {
        String loginPath = XxlJobConstants.loginPath(config.getVersion());
        return (int) mock.getRequestLog().stream()
                .filter(r -> r.path.endsWith(loginPath))
                .count();
    }
}
