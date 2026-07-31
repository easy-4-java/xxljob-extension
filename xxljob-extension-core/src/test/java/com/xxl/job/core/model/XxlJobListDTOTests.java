package com.xxl.job.core.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobInfoList} / {@link XxlJobGroupList} 分页 DTO 测试。
 */
class XxlJobListDTOTests {

    @Test
    void xxlJobInfoListSettersAndGetters() {
        XxlJobInfoList list = new XxlJobInfoList();
        list.setRecordsTotal(10);
        list.setRecordsFiltered(8);
        list.setData(Arrays.asList(new XxlJobInfo(), new XxlJobInfo()));
        assertThat(list.getRecordsTotal()).isEqualTo(10);
        assertThat(list.getRecordsFiltered()).isEqualTo(8);
        assertThat(list.getData()).hasSize(2);
    }

    @Test
    void xxlJobInfoListDefaultsAreNull() {
        XxlJobInfoList list = new XxlJobInfoList();
        assertThat(list.getRecordsTotal()).isNull();
        assertThat(list.getRecordsFiltered()).isNull();
        assertThat(list.getData()).isNull();
    }

    @Test
    void xxlJobGroupListSettersAndGetters() {
        XxlJobGroupList list = new XxlJobGroupList();
        list.setRecordsTotal(5);
        list.setRecordsFiltered(3);
        list.setData(Arrays.asList(new XxlJobGroup()));
        assertThat(list.getRecordsTotal()).isEqualTo(5);
        assertThat(list.getRecordsFiltered()).isEqualTo(3);
        assertThat(list.getData()).hasSize(1);
    }

    @Test
    void xxlJobGroupListDefaultsAreNull() {
        XxlJobGroupList list = new XxlJobGroupList();
        assertThat(list.getRecordsTotal()).isNull();
        assertThat(list.getRecordsFiltered()).isNull();
        assertThat(list.getData()).isNull();
    }

    @Test
    void toStringContainsClassNames() {
        assertThat(new XxlJobInfoList().toString()).contains("XxlJobInfoList");
        assertThat(new XxlJobGroupList().toString()).contains("XxlJobGroupList");
    }
}
