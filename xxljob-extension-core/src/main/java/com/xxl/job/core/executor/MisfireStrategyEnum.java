package com.xxl.job.core.executor;

/**
 * Enumeration of misfire strategies that define what happens when a scheduled trigger is missed (e.g., fire once, do nothing).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public enum MisfireStrategyEnum {

    /**
     * do nothing
     */
    DO_NOTHING,

    /**
     * fire once now
     */
    FIRE_ONCE_NOW;

    public static MisfireStrategyEnum match(String name, MisfireStrategyEnum defaultItem){
        for (MisfireStrategyEnum item: MisfireStrategyEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultItem;
    }

}
