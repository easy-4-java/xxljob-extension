package com.xxl.job.core.config;

import com.xxl.job.core.AdminVersion;
import lombok.Builder;
import lombok.Data;

/**
 * xxl-job-admin 连接配置（core 层轻量载体，独立于 Spring）。
 * <p>
 * 承载 {@link com.xxl.job.core.admin.DefaultXxlJobAdminClient} 所需的全部原始值，
 * 使 core 无需依赖 Spring 的 Properties POJO；由 spring 模块负责
 * Properties → Config 的组装。
 * </p>
 *
 * @see com.xxl.job.core.admin.DefaultXxlJobAdminClient
 */
@Data
@Builder
/**
 * Configuration properties for connecting to the xxl-job admin server, including base URL, credentials, and API version.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public class XxlJobAdminConfig {

    /**
     * 执行器通讯 TOKEN（请求头 XXL-JOB-ACCESS-TOKEN）。
     */
    private String accessToken;

    /**
     * 调度中心部署根地址，集群多地址用逗号分隔。
     */
    private String addresses;

    /**
     * 调度中心登录账号。
     */
    private String username;

    /**
     * 调度中心登录密码。
     */
    private String password;

    /**
     * 登录状态保持。默认 false：remember-me Cookie 的 Max-Age=2147483647 会导致解析失败。
     */
    @Builder.Default
    private boolean remember = false;

    /**
     * Admin Web API 协议版本（按路径选，非 admin Maven 版本号）。
     */
    @Builder.Default
    private AdminVersion version = AdminVersion.V2_X;

    /**
     * 以默认值构造（version=V2_X，remember=false）。
     */
    public static XxlJobAdminConfig of(String addresses, String username, String password) {
        return XxlJobAdminConfig.builder()
                .addresses(addresses)
                .username(username)
                .password(password)
                .build();
    }
}
