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
}
