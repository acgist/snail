package com.acgist.snail.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>日志工具</p>
 * 
 * @author acgist
 */
public final class Logger {

	/**
	 * <p>默认字符长度</p>
	 */
	private static final int DEFAULT_CAPACITY = 128;
	/**
	 * <p>时间格式</p>
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * <p>日志级别</p>
	 */
	private final int level;
	/**
	 * <p>日志名称</p>
	 */
	private final String name;
	/**
	 * <p>日志名称格式</p>
	 */
	private final String nameFormat;
	/**
	 * <p>日志系统名称</p>
	 */
	private final String system;
	/**
	 * <p>日志系统格式</p>
	 */
	private final String systemFormat;
	/**
	 * <p>日志上下文</p>
	 */
	private final List<LoggerAdapter> adapters;
	/**
	 * <p>日志单元</p>
	 */
	private final Map<String, Tuple> tupleMap;
	
	/**
	 * @param name 日志名称
	 */
	public Logger(String name) {
		this.name = name;
		this.nameFormat = String.format(" %s ", this.buildSimpleName(this.name));
		this.level = LoggerConfig.getLevel();
		this.system = LoggerConfig.getSystem();
		this.systemFormat = String.format("[%s] ", this.system);
		this.adapters = LoggerFactory.getAdapters();
		this.tupleMap = new ConcurrentHashMap<>();
	}
	
	/**
	 * <p>日志格式化</p>
	 * 
	 * @param level 级别
	 * @param format 格式
	 * @param args 参数
	 * 
	 * @return 日志
	 */
	private String format(Level level, String format, Object ... args) {
		final Tuple tuple = this.tupleMap.computeIfAbsent(format, Tuple::new);
		final StringBuilder builder = new StringBuilder(DEFAULT_CAPACITY);
		builder
			.append(this.systemFormat)
			.append(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
			.append(" [")
			.append(Thread.currentThread().getName())
			.append("] ")
			.append(level)
			.append(this.nameFormat);
		tuple.format(builder, args).append("\n");
		final Throwable throwable = tuple.throwable(args);
		if(throwable != null) {
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);
			builder
				.append(stringWriter.toString())
				.append("\n");
		}
		return builder.toString();
	}
	
	/**
	 * <p>判断是否支持日志级别</p>
	 * 
	 * @param level 级别
	 * 
	 * @return 是否支持
	 */
	private boolean isEnabled(Level level) {
		return this.level <= level.value();
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param format 日志
	 * @param args 参数
	 */
	private void log(Level level, String format, Object ... args) {
		if(this.isEnabled(level)) {
			final String message = this.format(level, format, args);
			final boolean error = level.value() >= Level.ERROR.value();
			// 减少判断
			if (error) {
				this.adapters.forEach(adapter -> adapter.errorOutput(message));
			} else {
				this.adapters.forEach(adapter -> adapter.output(message));
			}
		}
	}
	
	/**
	 * <p>简化日志名称</p>
	 * 
	 * @param name 完整名称
	 * 
	 * @return 简版名称
	 */
	public String buildSimpleName(String name) {
		int old;
		int index = 0;
		final char dot = '.';
		final StringBuilder builder = new StringBuilder();
		while((old = name.indexOf(dot, index)) != -1) {
			builder.append(name.substring(index, index + 1)).append(dot);
			index = old + 1;
		}
		builder.append(name.substring(index));
		return builder.toString();
	}
	
	public boolean isDebugEnabled() {
		return this.isEnabled(Level.DEBUG);
	}

	public void debug(String format, Object ... args) {
		this.log(Level.DEBUG, format, args);
	}

	public boolean isInfoEnabled() {
		return this.isEnabled(Level.INFO);
	}

	public void info(String format, Object ... args) {
		this.log(Level.INFO, format, args);
	}
	
	public boolean isWarnEnabled() {
		return this.isEnabled(Level.WARN);
	}

	public void warn(String format, Object ... args) {
		this.log(Level.WARN, format, args);
	}
	
	public boolean isErrorEnabled() {
		return this.isEnabled(Level.ERROR);
	}

	public void error(String format, Object ... args) {
		this.log(Level.ERROR, format, args);
	}
	
}
