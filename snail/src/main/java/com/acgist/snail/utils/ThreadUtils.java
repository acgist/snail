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
	
	/**
	 * <p>线程等待</p>
	 * <pre>
	 * synchronized(object) {
	 * 	ThreadUtils.wait(object, Duration.ofSeconds(number));
	 * }
	 * </pre>
	 * 
	 * @param object 等待对象：需要加锁
	 * @param timeout 等待时间：转为毫秒不宜过大
	 */
	public static final void wait(Object object, Duration timeout) {
		try {
			object.wait(timeout.toMillis());
		} catch (InterruptedException e) {
			LOGGER.debug("线程等待异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
}
