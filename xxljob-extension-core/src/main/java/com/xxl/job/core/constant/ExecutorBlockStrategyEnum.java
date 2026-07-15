package com.xxl.job.core.constant;

/**
 * 阻塞处理策略枚举。
 * <p>
 * 与 xxl-job-core 官方 <code>ExecutorBlockStrategyEnum</code> 在 {@code com.xxl.job.core.constant} 包路径
 * 的同名枚举保持同结构、同语义、同取值，在 extension 自己的源代码中重新定义。
 * 之所以不能在 extension 中直接引用库的该枚举，是因为：
 * </p>
 * <ul>
 *   <li>xxl-job-core 2.5.0 该枚举位于 {@code com.xxl.job.core.enums}（2.5.0 还没有 constant 包）；</li>
 *   <li>xxl-job-core 3.3.0+ 才把该枚举迁移到 {@code com.xxl.job.core.constant}。</li>
 * </ul>
 * <p>
 * 若直接在 extension 中 import 库的该枚举，三个 feature 分支会因包路径不同而产生源码差异，
 * 违背“除 pom.xml 外三个分支代码 100% 一致”的目标。因此 extension 自定义该枚举作为权威类型点，
 * 调用方导入的是 <code>com.xxl.job.core.constant.ExecutorBlockStrategyEnum</code>（与库同包路径的同名类），
 * 底层运行时的库枚举在该包路径下被本类遮蔽：所有调用走 extension 的枚举，
 * 不会触发 xxl-job-core 内部库类型与本类型不一致的兼容问题。
 * </p>
 */
public enum ExecutorBlockStrategyEnum {

    /**
     * serial execution
     */
    SERIAL_EXECUTION("Serial execution"),

    /**
     * discard later
     */
    DISCARD_LATER("Discard Later"),

    /**
     * cover early
     */
    COVER_EARLY("Cover Early");

    private String title;

    ExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 按名称匹配，未命中时返回默认项。
     *
     * @param name         枚举名
     * @param defaultItem  默认项
     * @return             匹配到的枚举项；未匹配返回 defaultItem
     */
    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
