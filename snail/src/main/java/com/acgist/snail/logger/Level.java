package com.acgist.snail.logger;

/**
 * 日志级别
 * 
 * @author acgist
 */
public enum Level {

    /**
     * DEBUG
     */
    DEBUG(100, "DEBUG"),
    /**
     * INFO
     */
    INFO(200, "INFO"),
    /**
     * WARN
     */
    WARN(300, "WARN"),
    /**
     * ERROR
     */
    ERROR(400, "ERROR"),
    /**
     * OFF
     */
    OFF(999, "OFF");
    
    /**
     * 级别
     */
    private final int value;
    /**
     * 名称
     */
    private final String name;
    
    /**
     * @param value 级别
     * @param name  名称
     */
    private Level(int value, String name) {
        this.value = value;
        this.name  = name;
    }
    
    /**
     * @return 级别
     */
    public final int value() {
        return this.value;
    }

    /**
     * @param name 名称
     * 
     * @return 级别
     */
    public static final Level of(String name) {
        final Level[] values = Level.values();
        for (Level level : values) {
            if(level.name.equalsIgnoreCase(name)) {
                return level;
            }
        }
        return Level.INFO;
    }
    
}
