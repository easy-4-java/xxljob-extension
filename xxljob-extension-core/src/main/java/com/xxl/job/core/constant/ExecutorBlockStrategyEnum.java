package com.xxl.job.core.constant;

/**
 * Enumeration of executor block strategies that define behavior when a job trigger arrives while the executor is already busy.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
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
