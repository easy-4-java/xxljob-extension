package com.xxl.job.core.admin;

import com.xxl.job.core.AdminVersion;
import com.xxl.job.core.XxlJobConstants;
import com.xxl.job.core.XxlJobTemplate;
import com.xxl.job.core.config.XxlJobAdminConfig;
import com.xxl.job.core.model.ReturnT;
import com.xxl.job.core.model.XxlJobGroupList;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 针对真实 xxl-job-admin（Docker）的只读集成测试。
 * <p>
 * 使用方法：
 * <pre>
 * docker compose -f src/test/resources/docker-compose.yml up -d
 * # 等待 xxl-job-admin 启动完成（约 30 秒）
 * XXL_JOB_ADMIN_URL=http://localhost:8080/xxl-job-admin mvn verify
 * docker compose -f src/test/resources/docker-compose.yml down -v
 * </pre>
 * <p>
 * 通过环境变量 {@code XXL_JOB_ADMIN_URL} 控制是否执行；
 * 未设置时整个测试类被跳过（不影响 CI 默认构建）。
 * </p>
 * <p>
 * <b>设计原则：只验证只读路径（login + pageList + 路径常量），
 * 避免写入操作触发 xxl-job-admin 在非 amd64 平台上的字符集兼容性细节、
 * 以及 admin "至少保留一个执行器" 的强约束。</b>
 * </p>
 */
@DisplayName("xxl-job-admin 真实 Docker 集成测试（只读）")
@EnabledIfEnvironmentVariable(named = "XXL_JOB_ADMIN_URL", matches = ".+")
class XxlJobAdminRealDockerIntegrationTests {

    private static UnirestInstance unirest;
    private static XxlJobTemplate template;
    private static AdminVersion version;

    @BeforeAll
    static void setUp() {
        String adminUrl = System.getenv("XXL_JOB_ADMIN_URL");
        String username = System.getProperty("xxl.admin.username", "admin");
        String password = System.getProperty("xxl.admin.password", "123456");
        String accessToken = System.getProperty("xxl.admin.token", "test_token_2026");
        String versionStr = System.getProperty("xxl.admin.version",
                System.getenv().getOrDefault("XXL_JOB_ADMIN_VERSION", "V2_X"));

        System.out.println("=== 真实 Admin Docker 集成测试 ===");
        System.out.println("Admin URL: " + adminUrl);
        System.out.println("Version:   " + versionStr);

        version = AdminVersion.valueOf(versionStr);

        unirest = Unirest.spawnInstance();
        unirest.config()
                .connectTimeout(15_000)
                .enableCookieManagement(true)
                .followRedirects(true);

        XxlJobAdminConfig config = XxlJobAdminConfig.builder()
                .accessToken(accessToken)
                .addresses(adminUrl)
                .username(username)
                .password(password)
                .remember(true)
                .version(version)
                .build();

        XxlJobAdminClient client = new DefaultXxlJobAdminClient(unirest, config);
        template = new XxlJobTemplate(client);

        ReturnT<String> loginResult = template.login(username, password, true);
        assertThat(loginResult.getCode())
                .as("登录应成功: " + loginResult.getMsg())
                .isEqualTo(ReturnT.SUCCESS_CODE);
        System.out.println("登录成功");
    }

    @AfterAll
    static void tearDown() {
        if (unirest != null) {
            Unirest.shutDown();
        }
    }

    // ======================== 验证类 ========================

    @Test
    @DisplayName("1. 登录 admin 后查询执行器列表（V2/V3 通用）")
    void loginAndQueryJobGroups() {
        ReturnT<XxlJobGroupList> result = template.jobInfoGroupList(0, 10);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent()).isNotNull();
        System.out.println("  ✓ 查询执行器列表成功, total=" +
                (result.getContent().getRecordsTotal() != null ? result.getContent().getRecordsTotal() : "n/a"));
    }

    @Test
    @DisplayName("2. 查询执行器列表（带 appname 过滤）")
    void queryJobGroupsWithFilter() {
        ReturnT<XxlJobGroupList> result = template.jobInfoGroupList(0, 10, "", "");
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent()).isNotNull();
        System.out.println("  ✓ 带过滤参数查询成功");
    }

    @Test
    @DisplayName("3. 验证版本感知路径常量正确")
    void verifyVersionAwarePathConstants() {
        // 验证每个版本对应的路径能正确给出
        assertThat(XxlJobConstants.loginPath(version)).isNotEmpty();
        assertThat(XxlJobConstants.logoutPath(version)).isNotEmpty();
        assertThat(XxlJobConstants.jobGroupSavePath(version)).isNotEmpty();
        assertThat(XxlJobConstants.jobGroupRemovePath(version)).isNotEmpty();
        assertThat(XxlJobConstants.jobInfoAddPath(version)).isNotEmpty();
        assertThat(XxlJobConstants.jobInfoRemovePath(version)).isNotEmpty();

        // 验证不同版本登录路径不同
        if (version.usesV3Login()) {
            assertThat(XxlJobConstants.loginPath(version)).startsWith("/auth/");
        } else {
            assertThat(XxlJobConstants.loginPath(version)).isEqualTo("/login");
        }
        if (version.usesV3FullApi()) {
            assertThat(XxlJobConstants.jobGroupSavePath(version)).isEqualTo("/jobgroup/insert");
            assertThat(XxlJobConstants.jobGroupRemovePath(version)).isEqualTo("/jobgroup/delete");
        } else {
            assertThat(XxlJobConstants.jobGroupSavePath(version)).isEqualTo("/jobgroup/save");
            assertThat(XxlJobConstants.jobGroupRemovePath(version)).isEqualTo("/jobgroup/remove");
        }
        System.out.println("  ✓ 版本 " + version + " 路径常量验证通过");
        System.out.println("    login:      " + XxlJobConstants.loginPath(version));
        System.out.println("    logout:     " + XxlJobConstants.logoutPath(version));
        System.out.println("    group+:     " + XxlJobConstants.jobGroupSavePath(version));
        System.out.println("    group-:     " + XxlJobConstants.jobGroupRemovePath(version));
        System.out.println("    job+:       " + XxlJobConstants.jobInfoAddPath(version));
        System.out.println("    job-:       " + XxlJobConstants.jobInfoRemovePath(version));
    }

    @Test
    @DisplayName("4. 校验构建的访问 URL 正确")
    void verifyBuildUrl() throws Exception {
        // 通过反射取出 Template 持有的 adminClient
        java.lang.reflect.Field f = com.xxl.job.core.XxlJobTemplate.class.getDeclaredField("adminClient");
        f.setAccessible(true);
        XxlJobAdminClient client = (XxlJobAdminClient) f.get(template);

        String adminUrl = System.getenv("XXL_JOB_ADMIN_URL");
        String fullUrl = client.buildUrl("/jobinfo/pageList");
        assertThat(fullUrl).isEqualTo(adminUrl + "/jobinfo/pageList");

        assertThat(client.version()).isEqualTo(version);
        assertThat(client.isV3()).isEqualTo(version.usesV3FullApi());
        System.out.println("  ✓ client 接口方法验证通过");
    }

    @Test
    @DisplayName("5. 多次连续只读查询（验证 session 保持）")
    void readOnlySessionsStable() {
        ReturnT<XxlJobGroupList> r1 = template.jobInfoGroupList(0, 10);
        ReturnT<XxlJobGroupList> r2 = template.jobInfoGroupList(0, 5);
        ReturnT<XxlJobGroupList> r3 = template.jobInfoGroupList(0, 20);

        assertThat(r1.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(r2.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(r3.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        System.out.println("  ✓ 多次只读会话稳定（无 302 重登）");
    }
}
