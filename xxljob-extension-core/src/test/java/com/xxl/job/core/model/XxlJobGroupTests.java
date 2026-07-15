package com.xxl.job.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobGroup} 执行器组模型测试，特别覆盖 {@code getRegistryList}
 * 根据 {@code addressList} 切片填充 {@code registryList} 的行为。
 */
class XxlJobGroupTests {

    @Test
    void settersAndGettersRoundTrip() {
        XxlJobGroup g = new XxlJobGroup();
        g.setId(1);
        g.setAppName("xxl-job-executor");
        g.setTitle("主执行器");
        g.setOrder(2);
        g.setAddressType(0);
        g.setAddressList("http://127.0.0.1:9999,http://127.0.0.2:9999");

        assertThat(g.getId()).isEqualTo(1);
        assertThat(g.getAppName()).isEqualTo("xxl-job-executor");
        assertThat(g.getTitle()).isEqualTo("主执行器");
        assertThat(g.getOrder()).isEqualTo(2);
        assertThat(g.getAddressType()).isZero();
        assertThat(g.getAddressList()).isEqualTo("http://127.0.0.1:9999,http://127.0.0.2:9999");
    }

    @Test
    void registryListSplitsCommaSeparatedAddressList() {
        XxlJobGroup g = new XxlJobGroup();
        g.setAddressList("a,b,c");
        assertThat(g.getRegistryList()).containsExactly("a", "b", "c");
    }

    @Test
    void registryListReturnsNullWhenAddressListIsNull() {
        assertThat(new XxlJobGroup().getRegistryList()).isNull();
    }

    @Test
    void registryListReturnsNullWhenAddressListIsBlank() {
        XxlJobGroup g = new XxlJobGroup();
        g.setAddressList("   ");
        assertThat(g.getRegistryList()).isNull();
    }

    @Test
    void registryListReturnsNullWhenAddressListIsEmpty() {
        XxlJobGroup g = new XxlJobGroup();
        g.setAddressList("");
        assertThat(g.getRegistryList()).isNull();
    }

    @Test
    void registryListReturnsEqualContentAcrossCalls() {
        XxlJobGroup g = new XxlJobGroup();
        g.setAddressList("x,y");
        List<String> first = g.getRegistryList();
        List<String> second = g.getRegistryList();
        assertThat(first).containsExactly("x", "y");
        assertThat(second).containsExactly("x", "y");
    }

    @Test
    void toStringIncludesClassName() {
        assertThat(new XxlJobGroup().toString()).contains("XxlJobGroup");
    }
}
