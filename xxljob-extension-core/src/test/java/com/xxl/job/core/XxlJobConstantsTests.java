package com.xxl.job.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobConstants} 静态常量 + 版本感知路径表测试。
 */
class XxlJobConstantsTests {

    @Test
    void publicPathConstantsAreStable() {
        assertThat(XxlJobConstants.JOBGROUP_PAGELIST).isEqualTo("/jobgroup/pageList");
        assertThat(XxlJobConstants.JOBGROUP_UPDATE).isEqualTo("/jobgroup/update");
        assertThat(XxlJobConstants.JOBGROUP_GET).isEqualTo("/jobgroup/loadById");
        assertThat(XxlJobConstants.JOBINFO_EXECUTOR_LIST).isEqualTo("/jobinfo/executorList");
        assertThat(XxlJobConstants.JOBINFO_PAGELIST).isEqualTo("/jobinfo/pageList");
        assertThat(XxlJobConstants.JOBINFO_UPDATE).isEqualTo("/jobinfo/update");
        assertThat(XxlJobConstants.JOBINFO_STOP).isEqualTo("/jobinfo/stop");
        assertThat(XxlJobConstants.JOBINFO_START).isEqualTo("/jobinfo/start");
        assertThat(XxlJobConstants.JOBINFO_TRIGGER).isEqualTo("/jobinfo/trigger");
    }

    @Test
    void cookieNamesAreStableAndDeprecatedAliasIsIdentity() {
        assertThat(XxlJobConstants.COOKIE_LOGIN_IDENTITY).isEqualTo("XXL_JOB_LOGIN_IDENTITY");
        assertThat(XxlJobConstants.COOKIE_LOGIN_TOKEN).isEqualTo("xxl_job_login_token");
        assertThat(XxlJobConstants.XXL_RPC_COOKIE).isEqualTo(XxlJobConstants.COOKIE_LOGIN_IDENTITY);
        assertThat(XxlJobConstants.XXL_RPC_ACCESS_TOKEN).isEqualTo("XXL-JOB-ACCESS-TOKEN");
    }

    @Test
    void defaultHandlerAndGlueTypeDefaults() {
        assertThat(XxlJobConstants.DEFAULT_HTTP_JOB_HANDLER).isEqualTo("evaluationHttpJobHandler");
        assertThat(XxlJobConstants.DEFAULT_GLUE_TYPE).isEqualTo("BEAN");
    }

    @Test
    void xxRpcCookieIsMarkedDeprecated() throws NoSuchFieldException {
        assertThat(java.lang.reflect.Modifier.isStatic(java.lang.reflect.Modifier.fieldModifiers()
                & XxlJobConstants.class.getDeclaredField("XXL_RPC_COOKIE").getModifiers())).isTrue();
        @SuppressWarnings("deprecation")
        boolean deprecated = XxlJobConstants.class.getDeclaredField("XXL_RPC_COOKIE")
                .isAnnotationPresent(Deprecated.class);
        assertThat(deprecated).isTrue();
    }

    @Test
    void privateConstructorIsInaccessible() throws Exception {
        var ctor = XxlJobConstants.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertThat(java.lang.reflect.Modifier.isPrivate(ctor.getModifiers())).isTrue();
        // 即使强制反射创建实例也应成功（确认可见性），不再抛错
        ctor.newInstance();
    }

    // ===== loginPath =====

    @Test
    void loginPathForV2UsesRoot() {
        assertThat(XxlJobConstants.loginPath(AdminVersion.V2_X)).isEqualTo("/login");
    }

    @Test
    void loginPathForV3_2AndV3_UseAuth() {
        assertThat(XxlJobConstants.loginPath(AdminVersion.V3_2_X)).isEqualTo("/auth/doLogin");
        assertThat(XxlJobConstants.loginPath(AdminVersion.V3_X)).isEqualTo("/auth/doLogin");
    }

    @Test
    void loginPathFallbackForNullUsesV2() {
        assertThat(XxlJobConstants.loginPath(null)).isEqualTo("/login");
    }

    // ===== logoutPath =====

    @Test
    void logoutPathByVersion() {
        assertThat(XxlJobConstants.logoutPath(AdminVersion.V2_X)).isEqualTo("/logout");
        assertThat(XxlJobConstants.logoutPath(AdminVersion.V3_2_X)).isEqualTo("/auth/logout");
        assertThat(XxlJobConstants.logoutPath(AdminVersion.V3_X)).isEqualTo("/auth/logout");
        assertThat(XxlJobConstants.logoutPath(null)).isEqualTo("/logout");
    }

    // ===== jobGroupSavePath =====

    @Test
    void jobGroupSavePathByVersion() {
        assertThat(XxlJobConstants.jobGroupSavePath(AdminVersion.V2_X)).isEqualTo("/jobgroup/save");
        assertThat(XxlJobConstants.jobGroupSavePath(AdminVersion.V3_2_X)).isEqualTo("/jobgroup/save");
        assertThat(XxlJobConstants.jobGroupSavePath(AdminVersion.V3_X)).isEqualTo("/jobgroup/insert");
        assertThat(XxlJobConstants.jobGroupSavePath(null)).isEqualTo("/jobgroup/save");
    }

    // ===== jobGroupRemovePath =====

    @Test
    void jobGroupRemovePathByVersion() {
        assertThat(XxlJobConstants.jobGroupRemovePath(AdminVersion.V2_X)).isEqualTo("/jobgroup/remove");
        assertThat(XxlJobConstants.jobGroupRemovePath(AdminVersion.V3_2_X)).isEqualTo("/jobgroup/remove");
        assertThat(XxlJobConstants.jobGroupRemovePath(AdminVersion.V3_X)).isEqualTo("/jobgroup/delete");
        assertThat(XxlJobConstants.jobGroupRemovePath(null)).isEqualTo("/jobgroup/remove");
    }

    // ===== jobInfoAddPath =====

    @Test
    void jobInfoAddPathByVersion() {
        assertThat(XxlJobConstants.jobInfoAddPath(AdminVersion.V2_X)).isEqualTo("/jobinfo/add");
        assertThat(XxlJobConstants.jobInfoAddPath(AdminVersion.V3_2_X)).isEqualTo("/jobinfo/add");
        assertThat(XxlJobConstants.jobInfoAddPath(AdminVersion.V3_X)).isEqualTo("/jobinfo/insert");
        assertThat(XxlJobConstants.jobInfoAddPath(null)).isEqualTo("/jobinfo/add");
    }

    // ===== jobInfoRemovePath =====

    @Test
    void jobInfoRemovePathByVersion() {
        assertThat(XxlJobConstants.jobInfoRemovePath(AdminVersion.V2_X)).isEqualTo("/jobinfo/remove");
        assertThat(XxlJobConstants.jobInfoRemovePath(AdminVersion.V3_2_X)).isEqualTo("/jobinfo/remove");
        assertThat(XxlJobConstants.jobInfoRemovePath(AdminVersion.V3_X)).isEqualTo("/jobinfo/delete");
        assertThat(XxlJobConstants.jobInfoRemovePath(null)).isEqualTo("/jobinfo/remove");
    }

    // ===== version 字段可写 =====

    @Test
    void jobInfoPageListPathIsMutable() {
        String original = XxlJobConstants.JOBINFO_PAGELIST;
        try {
            XxlJobConstants.JOBINFO_PAGELIST = "/custom/page";
            assertThat(XxlJobConstants.JOBINFO_PAGELIST).isEqualTo("/custom/page");
        } finally {
            XxlJobConstants.JOBINFO_PAGELIST = original;
        }
    }
}
