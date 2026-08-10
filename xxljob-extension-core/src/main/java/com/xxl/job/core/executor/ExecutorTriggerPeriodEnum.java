package com.xxl.job.core.executor;

/**
 * Enumeration of built-in trigger period presets (seconds, minutes, hours, etc.) for xxl-job scheduled tasks.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public enum ExecutorTriggerPeriodEnum {

    /**
     * 每周: 1
     */
    WEEK("每周"),

    /**
     * 每月: 2
     */
    MONTH("每月"),

    /**
     * 每天: 4
     */
    DAILY("每天");

    private final String desc;

    ExecutorTriggerPeriodEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return name() + " -> " + desc;
    }

}
