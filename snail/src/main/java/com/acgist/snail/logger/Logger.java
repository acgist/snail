package com.acgist.snail.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.acgist.snail.context.LoggerContext;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>日志工具</p>
 * 
 * @author acgist
 */
public final class Logger implements org.slf4j.Logger {

	/**
	 * <p>日志级别</p>
	 */
	private final int level;
	/**
	 * <p>日志名称</p>
	 */
	private final String name;
	/**
	 * <p>日志系统名称</p>
	 */
	private final String system;
	/**
	 * <p>日志上下文</p>
	 */
	private final LoggerContext context;
	
	/**
	 * @param name 日志名称
	 */
	public Logger(String name) {
		this.name = name;
		this.level = LoggerConfig.getLevelInt();
		this.system = LoggerConfig.getSystem();
		this.context = LoggerContext.getInstance();
	}
	
	/**
	 * <p>系统异常</p>
	 * 
	 * @param t 异常
	 */
	public static final void error(Throwable t) {
		try(FileOutputStream outputStream = new FileOutputStream(new File("logs/snail.logger.log"), true)) {
			final PrintWriter printWriter = new PrintWriter(outputStream);
			t.printStackTrace(printWriter);
			printWriter.flush();
		} catch (Exception e) {
			error(t);
		}
	}
	
	/**
	 * <p>日志格式化</p>
	 * 
	 * @param level 级别
	 * @param message 日志
	 * 
	 * @return 格式化日志
	 */
	private String format(Level level, FormattingTuple message) {
		final StringBuilder builder = new StringBuilder();
		builder
			.append("[").append(this.system).append("] ")
			.append(DateUtils.localDateTimeFormat(LocalDateTime.now())).append(" [")
			.append(Thread.currentThread().getName()).append("] ")
			.append(level.name()).append(" ")
			.append(this.name).append(" ")
			.append(message.getMessage()).append("\n");
		final Throwable throwable = message.getThrowable();
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
	 * <p>输出日志</p>
	 * 
	 * @param level 级别
	 * @param message 日志
	 */
	private void output(Level level, FormattingTuple message) {
		this.context.output(level, this.format(level, message));
	}
	
	/**
	 * <p>判断是否支持日志级别</p>
	 * 
	 * @param level 级别
	 * 
	 * @return 是否支持
	 */
	private boolean isEnabled(Level level) {
		return this.level <= level.toInt();
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param message 日志
	 */
	private void log(Level level, String message) {
		if(this.isEnabled(level)) {
			this.output(level, new FormattingTuple(message));
		}
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param format 日志
	 * @param arg 参数
	 */
	private void log(Level level, String format, Object arg) {
		if(this.isEnabled(level)) {
			this.output(level, MessageFormatter.format(format, arg));
		}
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param format 日志
	 * @param arga 参数
	 * @param argb 参数
	 */
	private void log(Level level, String format, Object arga, Object argb) {
		if(this.isEnabled(level)) {
			this.output(level, MessageFormatter.format(format, arga, argb));
		}
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
			this.output(level, MessageFormatter.arrayFormat(format, args));
		}
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param message 日志
	 * @param t 异常
	 */
	private void log(Level level, String message, Throwable t) {
		if(this.isEnabled(level)) {
			this.output(level, MessageFormatter.format(message, t));
		}
	}
	
	/**
	 * <p>判断是否支持日志级别</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * 
	 * @return 是否支持
	 */
	private boolean isEnabled(Level level, Marker marker) {
		return this.isEnabled(level);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * @param message 日志
	 */
	private void log(Level level, Marker marker, String message) {
		this.log(level, message);
	}

	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * @param format 日志
	 * @param arg 参数
	 */
	private void log(Level level, Marker marker, String format, Object arg) {
		this.log(level, format, arg);
	}

	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * @param format 日志
	 * @param arga 参数
	 * @param argb 参数
	 */
	private void log(Level level, Marker marker, String format, Object arga, Object argb) {
		this.log(level, format, arga, argb);
	}

	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * @param format 日志
	 * @param args 参数
	 */
	private void log(Level level, Marker marker, String format, Object ... args) {
		this.log(level, format, args);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param level 级别
	 * @param marker 标记
	 * @param message 日志
	 * @param t 异常
	 */
	private void log(Level level, Marker marker, String message, Throwable t) {
		this.log(level, message, t);
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.isEnabled(Level.TRACE);
	}

	@Override
	public void trace(String message) {
		this.log(Level.TRACE, message);
	}

	@Override
	public void trace(String format, Object arg) {
		this.log(Level.TRACE, format, arg);
	}

	@Override
	public void trace(String format, Object arga, Object argb) {
		this.log(Level.TRACE, format, arga, argb);
	}

	@Override
	public void trace(String format, Object ... args) {
		this.log(Level.TRACE, format, args);
	}

	@Override
	public void trace(String message, Throwable t) {
		this.log(Level.TRACE, message, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return this.isEnabled(Level.TRACE, marker);
	}

	@Override
	public void trace(Marker marker, String message) {
		this.log(Level.TRACE, marker, message);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		this.log(Level.TRACE, marker, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arga, Object argb) {
		this.log(Level.TRACE, marker, format, arga, argb);
	}

	@Override
	public void trace(Marker marker, String format, Object ... args) {
		this.log(Level.TRACE, marker, format, args);
	}

	@Override
	public void trace(Marker marker, String message, Throwable t) {
		this.log(Level.TRACE, marker, message, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.isEnabled(Level.DEBUG);
	}

	@Override
	public void debug(String message) {
		this.log(Level.DEBUG, message);
	}

	@Override
	public void debug(String format, Object arg) {
		this.log(Level.DEBUG, format, arg);
	}

	@Override
	public void debug(String format, Object arga, Object argb) {
		this.log(Level.DEBUG, format, arga, argb);
	}

	@Override
	public void debug(String format, Object ... args) {
		this.log(Level.DEBUG, format, args);
	}

	@Override
	public void debug(String message, Throwable t) {
		this.log(Level.DEBUG, message, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return this.isEnabled(Level.DEBUG, marker);
	}

	@Override
	public void debug(Marker marker, String message) {
		this.log(Level.DEBUG, message);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		this.log(Level.DEBUG, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arga, Object argb) {
		this.log(Level.DEBUG, format, arga, argb);
	}

	@Override
	public void debug(Marker marker, String format, Object ... args) {
		this.log(Level.DEBUG, format, args);
	}

	@Override
	public void debug(Marker marker, String message, Throwable t) {
		this.log(Level.DEBUG, message, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.isEnabled(Level.INFO);
	}

	@Override
	public void info(String message) {
		this.log(Level.INFO, message);
	}

	@Override
	public void info(String format, Object arg) {
		this.log(Level.INFO, format, arg);
	}

	@Override
	public void info(String format, Object arga, Object argb) {
		this.log(Level.INFO, format, arga, argb);
	}

	@Override
	public void info(String format, Object ... args) {
		this.log(Level.INFO, format, args);
	}

	@Override
	public void info(String message, Throwable t) {
		this.log(Level.INFO, message, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return this.isEnabled(Level.INFO, marker);
	}

	@Override
	public void info(Marker marker, String message) {
		this.log(Level.INFO, message);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		this.log(Level.INFO, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arga, Object argb) {
		this.log(Level.INFO, format, arga, argb);
	}

	@Override
	public void info(Marker marker, String format, Object ... args) {
		this.log(Level.INFO, format, args);
	}

	@Override
	public void info(Marker marker, String message, Throwable t) {
		this.log(Level.INFO, message, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.isEnabled(Level.WARN);
	}

	@Override
	public void warn(String message) {
		this.log(Level.WARN, message);
	}

	@Override
	public void warn(String format, Object arg) {
		this.log(Level.WARN, format, arg);
	}

	@Override
	public void warn(String format, Object ... args) {
		this.log(Level.WARN, format, args);
	}

	@Override
	public void warn(String format, Object arga, Object argb) {
		this.log(Level.WARN, format, arga, argb);
	}

	@Override
	public void warn(String message, Throwable t) {
		this.log(Level.WARN, message, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return this.isEnabled(Level.WARN, marker);
	}

	@Override
	public void warn(Marker marker, String message) {
		this.log(Level.WARN, message);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		this.log(Level.WARN, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arga, Object argb) {
		this.log(Level.WARN, format, arga, argb);
	}

	@Override
	public void warn(Marker marker, String format, Object ... args) {
		this.log(Level.WARN, format, args);
	}

	@Override
	public void warn(Marker marker, String message, Throwable t) {
		this.log(Level.WARN, message, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.isEnabled(Level.ERROR);
	}

	@Override
	public void error(String message) {
		this.log(Level.ERROR, message);
	}

	@Override
	public void error(String format, Object arg) {
		this.log(Level.ERROR, format, arg);
	}

	@Override
	public void error(String format, Object arga, Object argb) {
		this.log(Level.ERROR, format, arga, argb);
	}

	@Override
	public void error(String format, Object ... args) {
		this.log(Level.ERROR, format, args);
	}

	@Override
	public void error(String message, Throwable t) {
		this.log(Level.ERROR, message, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return this.isEnabled(Level.ERROR, marker);
	}

	@Override
	public void error(Marker marker, String message) {
		this.log(Level.ERROR, message);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		this.log(Level.ERROR, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arga, Object argb) {
		this.log(Level.ERROR, format, arga, argb);
	}

	@Override
	public void error(Marker marker, String format, Object ... args) {
		this.log(Level.ERROR, format, args);
	}

	@Override
	public void error(Marker marker, String message, Throwable t) {
		this.log(Level.ERROR, message, t);
	}
	
}
