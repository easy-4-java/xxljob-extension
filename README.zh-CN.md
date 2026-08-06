# xxljob-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-8-orange)](https://github.com/easy-4-java/xxljob-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

XXL-Job admin Web API 扩展 —— 基于 XXL-Job admin Web API 的纯 Java 扩展库，统一版本感知调用、Cookie 容错管理与多版本协议兼容；另提供带 Micrometer 指标的可选 Spring 集成模块。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 能力与状态](#2-能力与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本线与分支](#10-版本线与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`xxljob-extension` 是独立的 XXL-Job 扩展库，**不绑定 Spring Boot、Javalin、Quarkus 或 DDD4J**；按需选择 `core`（纯 Java）或 `spring`（Spring Framework 集成）。

**它是什么**

- 基于 Unirest 的 admin HTTP 客户端，禁用 Unirest 内置 Cookie 管理，改为手工解析。
- 版本感知 API 适配器，自动切换路径与参数：`V2_X`（2.x / 3.0 / 3.1）、`V3_2_X`（3.2 混合）、`V3_X`（3.3+ 完整 V3）。
- 会话自愈层：会话过期（302 或 HTML 登录页）时自动重置会话、重新登录并重试一次，业务无感。
- 执行器注解 `@XxlJobCron`（100% 替代 `@XxlJob`，可与 `@XxlJob` 组合），跨版本反射注册 `JobHandler`。
- `spring` 模块提供 Micrometer 集成（`MetricMethodJobHandler`、`XxlJobMetrics`），自动适配 `spring-boot-actuator`。

**它不是什么**

- 不是 `xxl-job-core` 的分支 —— 它通过 HTTP 调用 admin Web API，并复用 xxl-job 的模型类。
- 不是调度器 —— 调度仍由官方 XXL-Job 执行器 / admin 完成。

**典型场景**

| 场景 | 组件的作用 |
|:---|:---|
| 从代码管理执行器组 / 任务 | `XxlJobTemplate` 的 CRUD + start/stop/trigger |
| 一套代码兼容多个 admin 版本 | `AdminVersion` 路由（`V2_X` / `V3_2_X` / `V3_X`） |
| 启动时自动注册 `@XxlJobCron` 方法 | `XxlJobAutoBindingSpringExecutor`（spring 模块） |
| 通过 Prometheus 监控任务执行 | Micrometer 绑定器 `MetricMethodJobHandler` / `XxlJobMetrics`（spring 模块） |
| 无 Spring 环境使用 | `xxljob-extension-core` 零 Spring 依赖（构建期强制） |

## 2. 能力与状态

| 能力 | 状态 | 说明 |
|:---|:---|:---|
| Admin HTTP 客户端 | 稳定 | `DefaultXxlJobAdminClient` 基于 Unirest 统一调用；禁用内置 Cookie 管理，改为手工解析 |
| 多版本协议适配 | 稳定 | `AdminVersion` `V2_X` / `V3_2_X` / `V3_X` —— 登录、CRUD 路径、主键与分页参数自动切换 |
| Cookie 容错 | 稳定 | 规避 Apache HttpClient 对 remember-me Cookie 无效 `Expires` 的严格校验，避免被重定向到登录页 |
| 会话自愈 | 稳定 | 响应 302 / HTML 登录页时自动重置会话、重新登录并重试一次 |
| 业务门面 | 稳定 | `XxlJobTemplate`：登录、执行器组 CRUD、任务 CRUD、start/stop/trigger、防重添加 |
| 执行器注解 | 稳定 | `@XxlJobCron`（100% 替代 `@XxlJob`，可组合）+ `XxlJobHandlerRegistrar` 跨版本反射注册 |
| Micrometer 集成（spring） | 稳定 | `MetricMethodJobHandler` 为 handler 包装指标；`XxlJobMetrics` 暴露回调队列指标 |
| 多 JDK 版本线 | 稳定 | `feature/1.0.x`（JDK 8）、`feature/2.0.x`（JDK 17）、`feature/3.0.x`（JDK 21） |

## 3. 环境要求与兼容性

| 要求 | 版本 |
|:---|:---|
| JDK | 8+（`feature/1.0.x` 分支基线） |
| Maven | 3.0+ |
| XXL-Job admin | 2.x / 3.0 / 3.1（V2_X）、3.2（V3_2_X）、3.3+（V3_X）—— 通过 `AdminVersion` 选择，不绑定 admin 的 Maven 版本 |
| `xxl-job-core`（兼容基线） | 2.5.0 |
| Spring 集成（spring 模块） | `spring-boot-autoconfigure` 2.7.18、`micrometer-core` 1.9.17 |

**版本线矩阵**

| 分支 | JDK | 版本模式 |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

本文档描述 `feature/1.0.x` 版本线（当前版本：`1.0.x.20260630-SNAPSHOT`）。

## 4. 架构与模块

```text
   调用方
     |
     v
 XxlJobTemplate（业务门面）
     |
     v
 XxlJobAdminClient  <-- DefaultXxlJobAdminClient
                         |   XxlJobAdminPageListAdapter（分页适配）
                         |   XxlJobAdminCookieStore（手工 Cookie）
                         v
                    UnirestInstance（关闭 Cookie 管理、不跟随重定向）
                         |
                         v  V2_X | V3_2_X | V3_X 路径路由
                  xxl-job-admin Web API
```

**模块清单**

| 模块 | 类型 | 职责 |
|:---|:---|:---|
| `xxljob-extension-core` | 纯 Java | Admin HTTP 客户端、版本感知 API 适配、模型、`@XxlJobCron`、执行器枚举、工具 |
| `xxljob-extension-spring` | Spring 集成 | 执行器自动绑定（`@XxlJob` + `@XxlJobCron`）、Micrometer 指标 |

**包结构**（`core`：`com.xxl.job.core`，`spring`：`com.xxl.job.spring`）

| 包 | 内容 |
|:---|:---|
| `core`（根包） | `AdminVersion`、`XxlJobConstants`、`XxlJobTemplate` |
| `core.admin` | `XxlJobAdminClient`、`DefaultXxlJobAdminClient`、`XxlJobAdminCookieStore`、`XxlJobAdminHttpResponse`、`XxlJobAdminPageListAdapter` |
| `core.config` | `XxlJobAdminConfig` |
| `core.annotation` | `XxlJobCron` |
| `core.constant` / `core.executor` | `ExecutorBlockStrategyEnum`、`ExecutorRouteStrategyEnum`、`ExecutorTriggerPeriodEnum`、`MisfireStrategyEnum`、`ScheduleTypeEnum` |
| `core.model` | `ReturnT`、`XxlJobGroup`、`XxlJobGroupList`、`XxlJobInfo`、`XxlJobInfoList` |
| `core.util` | `XxlJobHandlerRegistrar`、`XxlJobHelper` |
| `spring`（根包） | `XxlJobAutoBindingSpringExecutor`、`XxlJobAutoBindingAndMetricsSpringExecutor` |
| `spring.metrics` | `MetricNames`、`MetricMethodJobHandler`、`XxlJobMetrics` |

## 5. 安装

> **假设**：制品目前通过项目私有 Maven 仓库（阿里云）与 GitHub Releases 分发；该库**尚未发布到 Maven Central**。若下列坐标无法解析，请在构建中配置私有仓库，或使用 `./mvnw install` 本地安装。

按需引入其中一个或两个模块：

**Maven**

```xml
<!-- 纯 Java：HTTP 客户端、模板、模型、注解、工具 -->
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>xxljob-extension-core</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>

<!-- Spring Framework 集成：执行器自动绑定、Micrometer 指标 -->
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>xxljob-extension-spring</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:xxljob-extension-core:1.0.x.20260630-SNAPSHOT'
implementation 'io.github.easy4j:xxljob-extension-spring:1.0.x.20260630-SNAPSHOT'
```

`core` 不依赖 Spring；`spring` 传递依赖 `core`。

## 6. 快速开始

**初始化 admin 客户端**

```java
UnirestInstance unirest = Unirest.spawnInstance();
unirest.config()
       .connectTimeout(10_000)
       // 关闭内置 Cookie，规避无效 Expires 解析失败
       .enableCookieManagement(false)
       // 不跟随 302，便于 postForm 识别未登录并重试登录
       .followRedirects(false);

XxlJobAdminConfig config = XxlJobAdminConfig.builder()
        .addresses("http://localhost:8080/xxl-job-admin")
        .username("admin")
        .password("123456")
        .version(AdminVersion.V3_X)
        .build();

XxlJobAdminClient client = new DefaultXxlJobAdminClient(unirest, config);
XxlJobTemplate template = new XxlJobTemplate(client);
```

**登录**

```java
ReturnT<String> login = template.login("admin", "123456", false);
if (login.getCode() == ReturnT.SUCCESS_CODE) {
    // 会话 Cookie 已缓存在 XxlJobAdminCookieStore，后续 postForm 透明携带
}
```

**执行器组 CRUD**

```java
XxlJobGroup group = new XxlJobGroup();
group.setAppName("my-executor");
group.setTitle("My Executor");
group.setAddressType(0);

ReturnT<String> add = template.addJobGroup(group);
Integer groupId = Integer.valueOf(add.getContent());

ReturnT<XxlJobGroupList> page = template.jobInfoGroupList(0, 10, "my-executor", null);
```

**任务 CRUD 与启动**

```java
XxlJobInfo job = new XxlJobInfo();
job.setJobGroup(groupId);
job.setJobDesc("daily-job");
job.setAuthor("ops");
job.setScheduleType(ScheduleTypeEnum.CRON.name());
job.setScheduleConf("0 0 3 * * ?");
job.setGlueType("BEAN");
job.setExecutorHandler("demoJobHandler");
job.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.LEAST_FREQUENTLY_USED.name());

ReturnT<String> addJob = template.addJob(job);
// addUniqueJob 会先按 jobDesc 去重再决定新增
// ReturnT<String> addJob = template.addUniqueJob(job);

Integer jobId = Integer.valueOf(addJob.getContent());

template.startJob(jobId);   // V2/V3_2 走 id；V3 走 ids[]
template.stopJob(jobId);
template.triggerJob(jobId, "{\"foo\":\"bar\"}");
```

**会话过期自愈**

`DefaultXxlJobAdminClient.postForm` 内部检测：响应 302 或内容为 HTML 登录页（非 JSON）时，自动重置会话、重新登录、重试一次，无需在业务代码中处理。

**使用 `@XxlJobCron` 自动绑定**（spring 模块）

`XxlJobAutoBindingSpringExecutor` 启动时扫描 `@Component` Bean 中的 `@XxlJob` 与 `@XxlJobCron` 方法，注册到执行器，并按 cron 元数据自动登录 admin 创建 / 更新任务：

```java
@Component
public class MyJobs {

    @XxlJobCron(value = "demoJobHandler", cron = "0/10 * * * * ?",
                desc = "示例", author = "ops")
    public void demo() {
        XxlJobHelper.log("hello from demo");
    }
}
```

如需启用 Micrometer 监控，将 `XxlJobAutoBindingSpringExecutor` 替换为 `XxlJobAutoBindingAndMetricsSpringExecutor`，handler 会自动接入 `MeterRegistry`。

## 7. 配置

**`XxlJobAdminConfig`（Builder）**

| 属性 | 默认值 | 说明 |
|:---|:---|:---|
| `addresses` | —（必填） | xxl-job-admin 控制台基础地址，如 `http://localhost:8080/xxl-job-admin` |
| `username` / `password` | —（必填） | `loginIfNeeded` 使用的 admin 登录凭据 |
| `version` | `V2_X` | `AdminVersion` 路由：`V2_X`（2.x/3.0/3.1）、`V3_2_X`（3.2 混合）、`V3_X`（3.3+） |

**Unirest 实例（必需设置）**

| 设置 | 值 | 原因 |
|:---|:---|:---|
| `connectTimeout` | 如 `10_000` | 请求超时（毫秒） |
| `enableCookieManagement(false)` | `false` | 规避 remember-me Cookie 无效 `Expires` 的严格校验 |
| `followRedirects(false)` | `false` | 让 `postForm` 识别未登录状态（302）并重试登录 |

**Micrometer 指标名（spring 模块）**

| 指标 | 类型 | 说明 |
|:---|:---|:---|
| `xxl.job.submitted` | Counter | 已提交的任务请求 |
| `xxl.job.running` | Gauge | 正在运行的任务请求 |
| `xxl.job.completed` | Counter | 已完成的任务请求 |
| `xxl.job.duration` | FunctionTimer | 任务执行耗时 |
| `xxl.job.queue.size` | Gauge | 回调队列大小（xxl-job-core 3.4+ 内部队列被移除后不可用，跳过并记录 warning） |

## 8. 核心用法 / API

**`XxlJobTemplate` 业务 API**

| 分组 | 方法 | 返回类型 |
|:---|:---|:---|
| 认证 | `login`、`logout` | `ReturnT<String>` |
| 执行器组 | `jobInfoGroupList`、`jobInfoGroup`、`addJobGroup`、`updateJobGroup`、`removeJobGroup` | `ReturnT<XxlJobGroupList>` / `ReturnT<XxlJobGroup>` / `ReturnT<String>` |
| 任务 | `jobInfoList`、`addJob`、`addUniqueJob`、`updateJob`、`removeJob` | `ReturnT<XxlJobInfoList>` / `ReturnT<String>` |
| 任务控制 | `startJob`、`stopJob`、`triggerJob` | `ReturnT<String>` |
| 客户端 | `version`、`isV3`、`loginIfNeeded`、`postForm`、`buildUrl` | 客户端接口方法 |

`ReturnT<T>` 字段：`code`（`SUCCESS_CODE=200` / `FAIL_CODE=500`）、`msg`、`content`（任意类型的业务数据）。

**多版本兼容**

| `AdminVersion` | 对应 admin | 登录路径 | CRUD 路径 | 删除路径 | 主键参数 | 分页参数 | Cookie 名 |
|:---|:---|:---|:---|:---|:---|:---|:---|
| `V2_X`（默认） | 2.x、3.0.0、3.1.x | `/login` | `save` / `add` / `remove` | `/jobinfo/remove` | `id` | `start` / `length` | `XXL_JOB_LOGIN_IDENTITY` |
| `V3_2_X` | 3.2.0 混合 | `/auth/doLogin` | `save` / `add` / `remove` | `/jobinfo/remove` | `id` | `start` / `length` | `xxl_job_login_token` |
| `V3_X` | 3.3.0+ 完整 V3 | `/auth/doLogin` | `insert` / `delete` | `/jobinfo/delete` | `ids[]` | `offset` / `pagesize` | `xxl_job_login_token` |

调用方只需在 `XxlJobAdminConfig` 中显式指定 `version`（默认 `V2_X`），其余调用同一套 API。

**设计约束**

- `core` 不含任何 Spring 类；enforcer 的 `ban-spring-dependencies` 规则会在 Spring 依赖混入 core 制品时使构建失败。
- `DefaultXxlJobAdminClient` 通过 `XxlJobAdminCookieStore` 手工管理 Cookie，只取 `name=value`，忽略 `Expires` / `Max-Age` 等可能无效的属性。
- `XxlJobHandlerRegistrar.registerJobHandler` 通过反射依次尝试 `registJobHandler` / `registryJobHandler` 方法名（兼容 xxl-job-core 2.5 ~ 3.4+）。
- 异常一律映射为 `ReturnT.FAIL_CODE` + 错误描述，调用方无需捕获受检异常。

## 9. 测试与构建

```bash
./mvnw clean test            # 在仓库根目录同时构建 core 与 spring 模块
./mvnw -pl xxljob-extension-spring test   # 仅跑 spring 模块测试（含 Mock server 多版本集成测试）
./mvnw clean verify          # 增加 JaCoCo 覆盖率门禁（行覆盖率 90%，haltOnFailure=false）
```

- `core` 测试包含覆盖全部三个协议版本的 `MockXxlJobAdminServer`，以及模型、枚举、Cookie 存储与注册器的单元测试。
- `XxlJobAdminRealDockerIntegrationTests` 仅在设置 `XXL_JOB_ADMIN_URL` 环境变量时运行（Docker Compose 文件与 `tables_xxl_job.sql` 位于测试资源中）。

## 10. 版本线与分支

| 分支 | JDK | 版本模式 | 说明 |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前版本线；`xxl-job-core` 2.5.0 基线 |
| `feature/2.0.x` | 17 | `2.0.x.*` | 下一代版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | 最新版本线 |

- 快照版本遵循 `1.0.x.yyyyMMdd-SNAPSHOT` 命名；发布版本以 `v{version}` 打标签，并通过项目私有仓库与 GitHub Releases 分发。
- `1.0.x` 是持续维护的 JDK 8 版本线；需要更新的 JDK 基线请升级到 `feature/2.0.x`（JDK 17）或 `feature/3.0.x`（JDK 21）。

## 11. 贡献与许可

欢迎贡献 —— 请在 GitHub 上提交 Issue 或 Pull Request。

本项目基于 **Apache License, Version 2.0** 许可发布。详见 [LICENSE](./LICENSE) 文件。
