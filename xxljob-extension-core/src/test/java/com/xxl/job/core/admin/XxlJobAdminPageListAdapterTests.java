package com.xxl.job.core.admin;

import com.xxl.job.core.AdminVersion;
import com.xxl.job.core.model.ReturnT;
import com.xxl.job.core.model.XxlJobGroupList;
import com.xxl.job.core.model.XxlJobInfoList;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobAdminPageListAdapter} 版本感知分页参数 + V2/V3 响应解析测试。
 */
class XxlJobAdminPageListAdapterTests {

    // ===== putPageParams =====

    @Test
    void v2PageParamsUseStartLength() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putPageParams(p, 10, 20, AdminVersion.V2_X);
        assertThat(p).containsEntry("start", 10).containsEntry("length", 20);
        assertThat(p).doesNotContainKey("offset").doesNotContainKey("pagesize");
    }

    @Test
    void v3PageParamsUseOffsetPagesize() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putPageParams(p, 10, 20, AdminVersion.V3_X);
        assertThat(p).containsEntry("offset", 10).containsEntry("pagesize", 20);
        assertThat(p).doesNotContainKey("start").doesNotContainKey("length");
    }

    @Test
    void v32PageParamsStillUseStartLength() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putPageParams(p, 0, 5, AdminVersion.V3_2_X);
        assertThat(p).containsEntry("start", 0).containsEntry("length", 5);
    }

    @Test
    void negativeStartClampedToZeroByMathMax() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putPageParams(p, -5, 10, AdminVersion.V2_X);
        assertThat(p).containsEntry("start", 0);
    }

    // ===== putIdParam =====

    @Test
    void v2IdParamUsesIdKey() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putIdParam(p, 42, AdminVersion.V2_X);
        assertThat(p).containsEntry("id", 42);
    }

    @Test
    void v3IdParamUsesIdsArray() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putIdParam(p, 42, AdminVersion.V3_X);
        assertThat(p).containsEntry("ids[]", "42");
    }

    @Test
    void v32IdParamUsesIdKey() {
        Map<String, Object> p = new HashMap<>();
        XxlJobAdminPageListAdapter.putIdParam(p, 1, AdminVersion.V3_2_X);
        assertThat(p).containsEntry("id", 1);
    }

    // ===== parseJobInfoList =====

    @Test
    void parseJobInfoListV2DataTables() {
        String body = "{\"recordsTotal\":2,\"recordsFiltered\":2,\"data\":[{\"id\":1},{\"id\":2}]}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V2_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getRecordsTotal()).isEqualTo(2);
        assertThat(result.getContent().getData()).hasSize(2);
    }

    @Test
    void parseJobInfoListV3PageModel() {
        String body = "{\"code\":200,\"data\":{\"total\":3,\"data\":[{\"id\":1,\"jobDesc\":\"a\"},"
                + "{\"id\":2,\"jobDesc\":\"b\"},{\"id\":3,\"jobDesc\":\"c\"}]}}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getRecordsTotal()).isEqualTo(3);
        assertThat(result.getContent().getData()).hasSize(3);
    }

    @Test
    void parseJobInfoListV3FailsWhenCodeNon200() {
        String body = "{\"code\":500,\"msg\":\"error\",\"data\":{}}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("error");
    }

    @Test
    void parseJobInfoListV3ReturnsErrorWhenDataIsNull() {
        String body = "{\"code\":200,\"data\":null}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.FAIL_CODE);
    }

    // ===== parseJobGroupList =====

    @Test
    void parseJobGroupListV3() {
        String body = "{\"code\":200,\"data\":{\"total\":1,\"data\":[{\"id\":10,\"appname\":\"test\"}]}}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V3_X);
        assertThat(result.getContent().getRecordsTotal()).isEqualTo(1);
        assertThat(result.getContent().getData().get(0).getAppName()).isEqualTo("test");
    }

    @Test
    void parseJobGroupListV2HandlesResponseWrapperMock() {
        // V2 响应也可能被包成 {code, data: {recordsTotal, ...}}（Mock 服务器场景）
        String body = "{\"code\":200,\"data\":{\"recordsTotal\":3,\"recordsFiltered\":3,\"data\":[{\"id\":1,\"appname\":\"g1\"}]}}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V2_X);
        assertThat(result.getContent().getRecordsTotal()).isEqualTo(3);
    }

    @Test
    void parseJobGroupListV3FailsWhenCodeNon200() {
        String body = "{\"code\":500,\"msg\":\"group error\"}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("group error");
    }

    @Test
    void parseJobGroupListV3ReturnsErrorWhenDataIsNull() {
        String body = "{\"code\":200,\"data\":null}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.FAIL_CODE);
    }

    @Test
    void parseJobGroupListV3ReturnsEmptyListWhenDataArrayIsEmpty() {
        String body = "{\"code\":200,\"data\":{\"total\":0,\"data\":[]}}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getData()).isEmpty();
    }

    @Test
    void parseJobInfoListV2HandlesWrappedResponse() {
        // V2 响应被包装成 {code, data: {recordsTotal, recordsFiltered, data: [...]}}
        String body = "{\"code\":200,\"data\":{\"recordsTotal\":5,\"recordsFiltered\":5,\"data\":[{\"id\":1}]}}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V2_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getRecordsTotal()).isEqualTo(5);
    }

    @Test
    void parseJobInfoListV2DirectDataTables() {
        // 直接 DataTables 格式（无 code 包装）
        String body = "{\"recordsTotal\":2,\"recordsFiltered\":2,\"data\":[{\"id\":1},{\"id\":2}]}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V2_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getData()).hasSize(2);
    }

    @Test
    void parseJobInfoListV3ReturnsEmptyListWhenDataArrayIsEmpty() {
        String body = "{\"code\":200,\"data\":{\"total\":0,\"data\":[]}}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getData()).isEmpty();
    }

    @Test
    void parseJobInfoListV3ReturnsErrorWhenDataArrayIsNull() {
        String body = "{\"code\":200,\"data\":null}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V3_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.FAIL_CODE);
    }

    @Test
    void parseJobInfoListV2HandlesResponseWithoutCodeField() {
        // V2 纯 DataTables 格式，无 code 字段
        String body = "{\"recordsTotal\":1,\"recordsFiltered\":1,\"data\":[{\"id\":1,\"jobDesc\":\"test\"}]}";
        ReturnT<XxlJobInfoList> result = XxlJobAdminPageListAdapter.parseJobInfoList(body, AdminVersion.V2_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getData().get(0).getJobDesc()).isEqualTo("test");
    }

    @Test
    void parseJobGroupListV2DirectDataTables() {
        String body = "{\"recordsTotal\":1,\"recordsFiltered\":1,\"data\":[{\"id\":1,\"appname\":\"direct\"}]}";
        ReturnT<XxlJobGroupList> result = XxlJobAdminPageListAdapter.parseJobGroupList(body, AdminVersion.V2_X);
        assertThat(result.getCode()).isEqualTo(ReturnT.SUCCESS_CODE);
        assertThat(result.getContent().getData().get(0).getAppName()).isEqualTo("direct");
    }

    @Test
    void buildJobGroupPageParamsReturnsCorrectStructure() {
        Map<String, Object> p = XxlJobAdminPageListAdapter.buildJobGroupPageParams(0, 10, "app", "title", AdminVersion.V2_X);
        assertThat(p).containsEntry("start", 0);
        assertThat(p).containsEntry("length", 10);
        assertThat(p).containsEntry("appname", "app");
        assertThat(p).containsEntry("title", "title");
    }

    @Test
    void buildJobInfoPageParamsReturnsCorrectStructure() {
        Map<String, Object> p = XxlJobAdminPageListAdapter.buildJobInfoPageParams(5, 20, 1, 1, "desc", "handler", "author", AdminVersion.V3_X);
        assertThat(p).containsEntry("offset", 5);
        assertThat(p).containsEntry("pagesize", 20);
        assertThat(p).containsEntry("jobGroup", 1);
        assertThat(p).containsEntry("triggerStatus", 1);
        assertThat(p).containsEntry("jobDesc", "desc");
        assertThat(p).containsEntry("executorHandler", "handler");
        assertThat(p).containsEntry("author", "author");
    }
}
