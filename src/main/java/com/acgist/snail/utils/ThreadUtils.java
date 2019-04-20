package com.acgist.snail.utils;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utils - 线程
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
	 * 线程等待
	 * wait会让出CPU执行其他的任务，线程池中同样会让出线程
	 */
	public static final void wait(Object obj, Duration timeout) {
		try {
			obj.wait(timeout.toMillis());
		} catch (InterruptedException e) {
			LOGGER.error("线程等待异常", e);
		}
	}
	
}
