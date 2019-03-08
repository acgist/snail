package com.acgist.snail.utils;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;

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
	 * 新建线程池工厂
	 * @param poolName 线程池名称
	 */
	public static final ThreadFactory newThreadFactory(String poolName) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				return SystemThreadContext.thread(poolName, runnable);
			}
		};
	}
	
}
