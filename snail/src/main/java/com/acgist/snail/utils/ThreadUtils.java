package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>线程工具</p>
 * 
 * @author acgist
 */
public final class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private ThreadUtils() {
	}
	
	/**
	 * <p>线程休眠</p>
	 * 
	 * @param millis 休眠时间（毫秒）
	 */
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.debug("线程休眠异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
}
