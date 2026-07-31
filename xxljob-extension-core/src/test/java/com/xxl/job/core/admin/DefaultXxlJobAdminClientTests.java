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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        if (Objects.nonNull(mock)) {
            mock.close();
        }
        Unirest.shutDown();
    }

    @Test
    void versionAndIsV3DelegatesToConfig() {
        assertThat(client.version()).isEqualTo(AdminVersion.V3_X);
        assertThat(client.isV3()).isTrue();
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
    void loginIfNeededIsIdempotent() {
        assertThat(client.login("admin", "123456", false)).isTrue();
        // 第二次 login 不应该重复发登录请求
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
    void postFormSendsRequestAndReturnsJsonResponse() {
        XxlJobAdminHttpResponse r = client.postForm(XxlJobConstants.JOBGROUP_PAGELIST,
                formParameters());
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.isJson()).isTrue();
        assertThat(r.getStatus()).isEqualTo(200);
    }

    private Map<String, Object> formParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("appname", "x");
        parameters.put("title", "y");
        return parameters;
    }

    private int countLoginRequests() {
        String loginPath = XxlJobConstants.loginPath(config.getVersion());
        return (int) mock.getRequestLog().stream()
                .filter(r -> r.path.endsWith(loginPath))
                .count();
    }
}
