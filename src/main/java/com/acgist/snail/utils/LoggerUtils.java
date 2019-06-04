package com.acgist.snail.utils;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * <p>日志工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class LoggerUtils {

	/**
	 * 关闭系统日志将缓存日志刷新到日志文件。
	 */
	public static final void shutdown() {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.stop();
	}
	
}
