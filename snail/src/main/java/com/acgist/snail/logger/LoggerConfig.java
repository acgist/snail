package com.acgist.snail.logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.event.Level;

/**
 * <p>日志配置</p>
 * 
 * @author acgist
 */
public final class LoggerConfig {

	private static final LoggerConfig INSTANCE = new LoggerConfig();
	
	public static final LoggerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String LOGGER_CONFIG = "/logger.properties";
	
	static {
		INSTANCE.init();
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private LoggerConfig() {
	}
	
	/**
	 * <p>日志级别</p>
	 */
	private int levelInt;
	/**
	 * <p>日志系统名称</p>
	 */
	private String system;
	/**
	 * <p>日志适配</p>
	 */
	private String adapter;
	/**
	 * <p>文件日志名称</p>
	 */
	private String fileName;
	/**
	 * <p>文件日志缓存（byte）</p>
	 */
	private int fileBuffer;
	/**
	 * <p>文件日志最大备份时间（天）</p>
	 */
	private int fileMaxDays;
	
	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		Properties properties = null;
		try(final var input = new InputStreamReader(LoggerConfig.class.getResourceAsStream(LOGGER_CONFIG), StandardCharsets.UTF_8)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			LoggerContext.error(e);
		}
		final Level level = this.of(properties.getProperty("logger.level"));
		this.levelInt = level.toInt();
		this.system = properties.getProperty("logger.system");
		this.adapter = properties.getProperty("logger.adapter");
		this.fileName = properties.getProperty("logger.file.name");
		this.fileBuffer = Integer.parseInt(properties.getProperty("logger.file.buffer", "8192"));
		this.fileMaxDays = Integer.parseInt(properties.getProperty("logger.file.max.days", "30"));
	}
	
	/**
	 * <p>日志级别转换</p>
	 * 
	 * @param levelValue 日志级别
	 * 
	 * @return 日志级别
	 */
	private Level of(String levelValue) {
		final Level[] levels = Level.values();
		for (Level level : levels) {
			if(level.name().equalsIgnoreCase(levelValue)) {
				return level;
			}
		}
		return Level.DEBUG; // 默认：DEBUG
	}
	
	/**
	 * <p>关闭日志</p>
	 */
	public static final void off() {
		INSTANCE.levelInt = Integer.MAX_VALUE;
	}
	
	/**
	 * <p>获取日志级别</p>
	 * 
	 * @return 日志级别
	 */
	public static final int getLevelInt() {
		return INSTANCE.levelInt;
	}
	
	/**
	 * <p>获取日志系统名称</p>
	 * 
	 * @return 日志系统名称
	 */
	public static final String getSystem() {
		return INSTANCE.system;
	}
	
	/**
	 * <p>获取日志适配</p>
	 * 
	 * @return 日志适配
	 */
	public static final String getAdapter() {
		return INSTANCE.adapter;
	}

	/**
	 * <p>获取文件日志名称</p>
	 * 
	 * @return 文件日志名称
	 */
	public static final String getFileName() {
		return INSTANCE.fileName;
	}

	/**
	 * <p>获取文件日志缓存（byte）</p>
	 * 
	 * @return 文件日志缓存（byte）
	 */
	public static final int getFileBuffer() {
		return INSTANCE.fileBuffer;
	}

	/**
	 * <p>获取文件日志最大备份时间（天）</p>
	 * 
	 * @return 文件日志最大备份时间（天）
	 */
	public static final int getFileMaxDays() {
		return INSTANCE.fileMaxDays;
	}

}
