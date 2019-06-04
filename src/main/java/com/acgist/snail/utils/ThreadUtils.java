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
public class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * 休眠
	 */
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("线程休眠异常");
		}
	}
	
	/**
	 * <p>线程等待</p>
	 * <p>wait会让出CPU执行其他的任务，线程池中同样会让出线程。</p>
	 * 
	 * @param timeout 注意不能设置过大，转换为毫秒时超过long最大值。
	 */
	public static final void wait(Object obj, Duration timeout) {
		try {
			obj.wait(timeout.toMillis());
		} catch (InterruptedException e) {
			LOGGER.error("线程等待异常", e);
		}
	}
	
}
