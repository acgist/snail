package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * <p>日志工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class LoggerUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtils.class);
	
	/**
	 * <p>关闭系统日志</p>
	 * <p>将缓存的日志数据写入到日志文件</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭系统日志");
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.stop();
	}

}
