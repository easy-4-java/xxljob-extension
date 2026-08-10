package com.xxl.job.core.admin;

import com.xxl.job.core.model.ReturnT;
import com.xxl.job.core.AdminVersion;

import java.util.Map;

/**
 * Contract for a client that communicates with the xxl-job admin server REST API.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public interface XxlJobAdminClient {

    /**
     * 当前配置的 admin 版本。
     */
    AdminVersion version();

    /**
     * 是否为完整 V3 admin API（3.3.0+）。
     */
    boolean isV3();

    /**
     * 登录 admin，成功时缓存 Cookie。
     */
    boolean login(String userName, String password, boolean remember);

    /**
     * 使用配置中的账号密码登录（若尚未登录）。
     */
    void loginIfNeeded();

    /**
     * 登出并清除 Cookie。
     */
    ReturnT<String> logout();

    /**
     * 向 admin 发送表单 POST（自动登录、session 过期自动重试一次）。
     *
     * @param pathSuffix 路径后缀，如 {@code /jobgroup/pageList}
     */
    XxlJobAdminHttpResponse postForm(String pathSuffix, Map<String, Object> paramMap);

    /**
     * 拼接完整 URL。
     */
    String buildUrl(String pathSuffix);

}
