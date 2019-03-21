package com.acgist.snail.utils;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * 日志工具
 */
public class LoggerUtils {

	/**
	 * 关闭系统日志刷新缓存
	 */
	public static final void shutdown() {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.stop();
	}
	
}
