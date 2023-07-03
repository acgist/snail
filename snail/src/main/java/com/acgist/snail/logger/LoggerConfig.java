package com.acgist.snail.logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 日志配置
 * 
 * @author acgist
 */
public final class LoggerConfig {

    private static final LoggerConfig INSTANCE = new LoggerConfig();
    
    public static final LoggerConfig getInstance() {
        return INSTANCE;
    }
    
    /**
     * 配置文件：{@value}
     */
    private static final String LOGGER_CONFIG = "/logger.properties";
    
    static {
        INSTANCE.init();
    }
    
    private LoggerConfig() {
    }
    
    /**
     * 日志级别
     */
    private int level;
    /**
     * 日志系统名称
     */
    private String system;
    /**
     * 日志适配
     */
    private String adapter;
    /**
     * 文件日志名称
     */
    private String fileName;
    /**
     * 文件日志缓存（byte）
     */
    private int fileBuffer;
    /**
     * 文件日志最大备份时间（天）
     */
    private int fileMaxDays;
    
    /**
     * 加载配置
     */
    private void init() {
        final Properties properties = new Properties();
        try(final Reader reader = new InputStreamReader(LoggerConfig.class.getResourceAsStream(LOGGER_CONFIG), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            LoggerFactory.error(e);
        }
        this.level       = Level.of(properties.getProperty("logger.level")).value();
        this.system      = properties.getProperty("logger.system");
        this.adapter     = properties.getProperty("logger.adapter");
        this.fileName    = properties.getProperty("logger.file.name");
        this.fileBuffer  = Integer.parseInt(properties.getProperty("logger.file.buffer", "8192"));
        this.fileMaxDays = Integer.parseInt(properties.getProperty("logger.file.max.days", "30"));
    }
    
    /**
     * 关闭日志
     */
    public static final void off() {
        INSTANCE.level = Level.OFF.value();
    }
    
    /**
     * @param level 日志级别
     */
    public static final void setLevel(Level level) {
        INSTANCE.level = level.value();
    }
    
    /**
     * @return 日志级别
     */
    public static final int getLevel() {
        return INSTANCE.level;
    }
    
    /**
     * @return 日志系统名称
     */
    public static final String getSystem() {
        return INSTANCE.system;
    }
    
    /**
     * @return 日志适配
     */
    public static final String getAdapter() {
        return INSTANCE.adapter;
    }

    /**
     * @return 文件日志名称
     */
    public static final String getFileName() {
        return INSTANCE.fileName;
    }

    /**
     * @return 文件日志缓存（byte）
     */
    public static final int getFileBuffer() {
        return INSTANCE.fileBuffer;
    }

    /**
     * @return 文件日志最大备份时间（天）
     */
    public static final int getFileMaxDays() {
        return INSTANCE.fileMaxDays;
    }

}
