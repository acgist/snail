package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * <p>日志工具</p>
 * 
 * @author acgist
 */
public final class LoggerUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private LoggerUtils() {
	}
	
	/**
	 * <p>关闭日志系统</p>
	 * <p>将日志缓存数据写入日志文件</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭日志系统");
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		if(context != null) {
			context.stop();
		}
	}

}
