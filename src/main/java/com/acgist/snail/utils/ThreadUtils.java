package com.acgist.snail.utils;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>线程工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * 线程休眠
	 */
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.debug("线程休眠异常");
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * <p>线程等待</p>
	 * 
	 * @param timeout 等待时间（注意：转为毫秒不宜过大）
	 */
	public static final void wait(Object obj, Duration timeout) {
		try {
			obj.wait(timeout.toMillis());
		} catch (InterruptedException e) {
			LOGGER.debug("线程等待异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
}
