# xxljob-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://github.com/easy-4-java/xxljob-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

XXL-Job admin Web API extension — a pure-Java extension library for the XXL-Job admin console with version-aware API invocation, Cookie fault tolerance and multi-version protocol compatibility, plus an optional Spring integration module with Micrometer metrics.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`xxljob-extension` is a standalone XXL-Job extension. It is **not bound to Spring Boot, Javalin, Quarkus or DDD4J**; you choose `core` (pure Java) or `spring` (Spring Framework integration) as needed.

**What it is**

- An admin HTTP client based on Unirest, with Unirest's built-in Cookie management disabled in favor of hand-rolled Cookie parsing.
- A version-aware API adapter that switches paths and parameters automatically: `V2_X` (2.x / 3.0 / 3.1), `V3_2_X` (3.2 hybrid) and `V3_X` (3.3+ full V3).
- A session self-healing layer: expired sessions (302 or HTML login page) trigger an automatic re-login and one retry, transparent to business code.
- An executor annotation `@XxlJobCron` (a 100% replacement for `@XxlJob`, combinable with it) with cross-version reflective `JobHandler` registration.
- Micrometer integration in the `spring` module (`MetricMethodJobHandler`, `XxlJobMetrics`), auto-adapting to `spring-boot-actuator`.

**What it is not**

- Not a fork of `xxl-job-core` — it calls the admin Web API over HTTP and reuses xxl-job's model classes.
- Not a scheduler — scheduling still runs on the official XXL-Job executor / admin.

**Typical scenarios**

| Scenario | How this component helps |
|:---|:---|
| Manage job groups / jobs from code | `XxlJobTemplate` CRUD + start/stop/trigger |
| Support multiple admin versions in one codebase | `AdminVersion` routing (`V2_X` / `V3_2_X` / `V3_X`) |
| Auto-register `@XxlJobCron` methods on startup | `XxlJobAutoBindingSpringExecutor` (spring module) |
| Monitor job execution from Prometheus | Micrometer binders `MetricMethodJobHandler` / `XxlJobMetrics` (spring module) |
| Operate without Spring | `xxljob-extension-core` has zero Spring dependencies (enforced at build time) |

## 2. Features & Status

| Capability | Status | Description |
|:---|:---|:---|
| Admin HTTP client | Stable | Unified Unirest-based invocation in `DefaultXxlJobAdminClient`; built-in Cookie management disabled, parsing done manually |
| Multi-version protocol | Stable | `AdminVersion` `V2_X` / `V3_2_X` / `V3_X` — login, CRUD paths, primary-key and pagination parameters switch automatically |
| Cookie fault tolerance | Stable | Handles invalid `Expires` on the `remember-me` Cookie that Apache HttpClient would reject, avoiding redirects to the login page |
| Session self-healing | Stable | On 302 / HTML login page responses, resets the session, re-logs in and retries once |
| Business facade | Stable | `XxlJobTemplate`: login, job-group CRUD, job CRUD, start/stop/trigger, deduplicated add |
| Executor annotation | Stable | `@XxlJobCron` (100% replaces `@XxlJob`, combinable) + cross-version reflective registration via `XxlJobHandlerRegistrar` |
| Micrometer integration (spring) | Stable | `MetricMethodJobHandler` wraps handlers with metrics; `XxlJobMetrics` exposes callback queue metrics |
| Multi-JDK lines | Stable | `feature/1.0.x` (JDK 8), `feature/2.0.x` (JDK 17), `feature/3.0.x` (JDK 21) |

## 3. Requirements & Compatibility

| Requirement | Version |
|:---|:---|
| JDK | 17+ (baseline of the `feature/2.0.x` branch) |
| Maven | 3.0+ |
| XXL-Job admin | 2.x / 3.0 / 3.1 (V2_X), 3.2 (V3_2_X), 3.3+ (V3_X) — selected via `AdminVersion`, not bound to the admin Maven version |
| `xxl-job-core` (compat baseline) | 2.5.0 |
| Spring integration (spring module) | `spring-boot-autoconfigure` 2.7.18, `micrometer-core` 1.9.17 |

**Version line matrix**

| Branch | JDK | Version pattern |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

This document describes the `feature/2.0.x` line (current version: `2.0.x.x.20260630-SNAPSHOT`).

## 4. Architecture & Modules

```text
   Caller
     |
     v
 XxlJobTemplate (business facade)
     |
     v
 XxlJobAdminClient  <-- DefaultXxlJobAdminClient
                         |   XxlJobAdminPageListAdapter (pagination)
                         |   XxlJobAdminCookieStore (manual cookie)
                         v
                    UnirestInstance (cookie mgmt off, no redirects)
                         |
                         v  V2_X | V3_2_X | V3_X path routing
                  xxl-job-admin Web API
```

**Module list**

| Module | Type | Responsibility |
|:---|:---|:---|
| `xxljob-extension-core` | Pure Java | Admin HTTP client, version-aware API adapter, models, `@XxlJobCron`, executor enums, utilities |
| `xxljob-extension-spring` | Spring integration | Executor auto-binding (`@XxlJob` + `@XxlJobCron`), Micrometer metrics |

**Package layout** (`core`: `com.xxl.job.core`, `spring`: `com.xxl.job.spring`)

| Package | Content |
|:---|:---|
| `core` (root) | `AdminVersion`, `XxlJobConstants`, `XxlJobTemplate` |
| `core.admin` | `XxlJobAdminClient`, `DefaultXxlJobAdminClient`, `XxlJobAdminCookieStore`, `XxlJobAdminHttpResponse`, `XxlJobAdminPageListAdapter` |
| `core.config` | `XxlJobAdminConfig` |
| `core.annotation` | `XxlJobCron` |
| `core.constant` / `core.executor` | `ExecutorBlockStrategyEnum`, `ExecutorRouteStrategyEnum`, `ExecutorTriggerPeriodEnum`, `MisfireStrategyEnum`, `ScheduleTypeEnum` |
| `core.model` | `ReturnT`, `XxlJobGroup`, `XxlJobGroupList`, `XxlJobInfo`, `XxlJobInfoList` |
| `core.util` | `XxlJobHandlerRegistrar`, `XxlJobHelper` |
| `spring` (root) | `XxlJobAutoBindingSpringExecutor`, `XxlJobAutoBindingAndMetricsSpringExecutor` |
| `spring.metrics` | `MetricNames`, `MetricMethodJobHandler`, `XxlJobMetrics` |

## 5. Installation

> **Assumption**: artifacts are currently distributed through the project's private Maven repository (Aliyun) and GitHub Releases; the library is **not yet published to Maven Central**. If the coordinates below cannot be resolved, either add the private repository to your build or install locally with `./mvnw install`.

Bring one or both modules as needed:

**Maven**

```xml
<!-- Pure Java: HTTP client, template, models, annotations, utils -->
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>xxljob-extension-core</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>

<!-- Spring Framework integration: executor auto-binding, Micrometer metrics -->
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>xxljob-extension-spring</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:xxljob-extension-core:2.0.x.x.20260630-SNAPSHOT'
implementation 'io.github.easy4j:xxljob-extension-spring:2.0.x.x.20260630-SNAPSHOT'
```

`core` does not depend on Spring; `spring` depends transitively on `core`.

## 6. Quick Start

**Initialize the admin client**

```java
UnirestInstance unirest = Unirest.spawnInstance();
unirest.config()
       .connectTimeout(10_000)
       // Disable the built-in Cookie handling to avoid invalid-Expires parse failures
       .enableCookieManagement(false)
       // Do not follow 302, so postForm can detect the logged-out state and retry login
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

**Login**

```java
ReturnT<String> login = template.login("admin", "123456", false);
if (login.getCode() == ReturnT.SUCCESS_CODE) {
    // The session Cookie is cached in XxlJobAdminCookieStore and carried transparently
}
```

**Job group CRUD**

```java
XxlJobGroup group = new XxlJobGroup();
group.setAppName("my-executor");
group.setTitle("My Executor");
group.setAddressType(0);

ReturnT<String> add = template.addJobGroup(group);
Integer groupId = Integer.valueOf(add.getContent());

ReturnT<XxlJobGroupList> page = template.jobInfoGroupList(0, 10, "my-executor", null);
```

**Job CRUD and control**

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
// addUniqueJob deduplicates by jobDesc before inserting
// ReturnT<String> addJob = template.addUniqueJob(job);

Integer jobId = Integer.valueOf(addJob.getContent());

template.startJob(jobId);   // V2/V3_2 uses id; V3 uses ids[]
template.stopJob(jobId);
template.triggerJob(jobId, "{\"foo\":\"bar\"}");
```

**Session self-healing**

`DefaultXxlJobAdminClient.postForm` internally detects a 302 response or an HTML login page (non-JSON), resets the session, re-logs in and retries once — no business code needed.

**Auto-binding with `@XxlJobCron`** (spring module)

`XxlJobAutoBindingSpringExecutor` scans `@Component` beans for `@XxlJob` and `@XxlJobCron` methods at startup, registers the handlers on the executor, and uses the cron metadata to log in to admin and create/update the jobs:

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

To enable Micrometer monitoring, replace `XxlJobAutoBindingSpringExecutor` with `XxlJobAutoBindingAndMetricsSpringExecutor` — handlers are then wired into the `MeterRegistry` automatically.

## 7. Configuration

**`XxlJobAdminConfig` (builder)**

| Property | Default | Description |
|:---|:---|:---|
| `addresses` | — (required) | Base URL of the xxl-job-admin console, e.g. `http://localhost:8080/xxl-job-admin` |
| `username` / `password` | — (required) | Admin login credentials used by `loginIfNeeded` |
| `version` | `V2_X` | `AdminVersion` routing: `V2_X` (2.x/3.0/3.1), `V3_2_X` (3.2 hybrid), `V3_X` (3.3+) |

**Unirest instance (required settings)**

| Setting | Value | Why |
|:---|:---|:---|
| `connectTimeout` | e.g. `10_000` | Request timeout in ms |
| `enableCookieManagement(false)` | `false` | Avoid strict validation of an invalid `Expires` on the remember-me Cookie |
| `followRedirects(false)` | `false` | Let `postForm` recognize the logged-out state (302) and retry login |

**Micrometer metric names (spring module)**

| Metric | Type | Description |
|:---|:---|:---|
| `xxl.job.submitted` | Counter | Submitted job requests |
| `xxl.job.running` | Gauge | Currently running job requests |
| `xxl.job.completed` | Counter | Completed job requests |
| `xxl.job.duration` | FunctionTimer | Job execution duration |
| `xxl.job.queue.size` | Gauge | Callback queue size (unavailable on xxl-job-core 3.4+, where the internal queue was removed — skipped with a warning) |

## 8. Core Usage / API

**`XxlJobTemplate` business API**

| Group | Methods | Return type |
|:---|:---|:---|
| Authentication | `login`, `logout` | `ReturnT<String>` |
| Job groups | `jobInfoGroupList`, `jobInfoGroup`, `addJobGroup`, `updateJobGroup`, `removeJobGroup` | `ReturnT<XxlJobGroupList>` / `ReturnT<XxlJobGroup>` / `ReturnT<String>` |
| Jobs | `jobInfoList`, `addJob`, `addUniqueJob`, `updateJob`, `removeJob` | `ReturnT<XxlJobInfoList>` / `ReturnT<String>` |
| Job control | `startJob`, `stopJob`, `triggerJob` | `ReturnT<String>` |
| Client | `version`, `isV3`, `loginIfNeeded`, `postForm`, `buildUrl` | Client interface methods |

`ReturnT<T>` fields: `code` (`SUCCESS_CODE=200` / `FAIL_CODE=500`), `msg`, `content` (business payload of any type).

**Multi-version compatibility**

| `AdminVersion` | Admin versions | Login path | CRUD paths | Delete path | Primary key | Pagination | Cookie name |
|:---|:---|:---|:---|:---|:---|:---|:---|
| `V2_X` (default) | 2.x, 3.0.0, 3.1.x | `/login` | `save` / `add` / `remove` | `/jobinfo/remove` | `id` | `start` / `length` | `XXL_JOB_LOGIN_IDENTITY` |
| `V3_2_X` | 3.2.0 hybrid | `/auth/doLogin` | `save` / `add` / `remove` | `/jobinfo/remove` | `id` | `start` / `length` | `xxl_job_login_token` |
| `V3_X` | 3.3.0+ full V3 | `/auth/doLogin` | `insert` / `delete` | `/jobinfo/delete` | `ids[]` | `offset` / `pagesize` | `xxl_job_login_token` |

Callers only set `version` in `XxlJobAdminConfig` (default `V2_X`); everything else uses one API surface.

**Design constraints**

- `core` has no Spring classes; an enforcer `ban-spring-dependencies` rule fails the build if Spring sneaks into the core artifact.
- `DefaultXxlJobAdminClient` manages Cookies via `XxlJobAdminCookieStore`, taking only `name=value` and ignoring potentially invalid attributes such as `Expires` / `Max-Age`.
- `XxlJobHandlerRegistrar.registerJobHandler` reflectively tries `registJobHandler` / `registryJobHandler` method names (compatible with xxl-job-core 2.5 ~ 3.4+).
- Exceptions are mapped to `ReturnT.FAIL_CODE` + an error description; callers do not need to catch checked exceptions.

## 9. Testing & Build

```bash
./mvnw clean test            # builds core and spring together at the repository root
./mvnw -pl xxljob-extension-spring test   # spring module tests only (incl. Mock-server multi-version tests)
./mvnw clean verify          # adds the JaCoCo coverage gate (90% line minimum, haltOnFailure=false)
```

- `core` tests include a `MockXxlJobAdminServer` covering all three protocol versions, plus unit tests for models, enums, Cookie store and the registrar.
- `XxlJobAdminRealDockerIntegrationTests` runs only when the `XXL_JOB_ADMIN_URL` environment variable is set (Docker Compose file and `tables_xxl_job.sql` are in the test resources).

## 10. Versioning & Branches

| Branch | JDK | Version pattern | Notes |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | Current line; `xxl-job-core` 2.5.0 baseline |
| `feature/2.0.x` | 17 | `2.0.x.*` | Next generation line |
| `feature/3.0.x` | 21 | `3.0.x.*` | Latest line |

- Snapshot versions follow the `1.0.x.yyyyMMdd-SNAPSHOT` scheme; releases are tagged `v{version}` and published through the project's private repository and GitHub Releases.
- The `1.0.x` line is the actively maintained JDK 8 line; upgrade to `feature/2.0.x` (JDK 17) or `feature/3.0.x` (JDK 21) for newer JDK baselines.

## 11. Contributing & License

Contributions are welcome — please open an issue or a pull request on GitHub.

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](./LICENSE) file for details.
