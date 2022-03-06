package com.acgist.snail.utils;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>线程工具</p>
 * <p>注意：wait会释放锁，sleep不释放锁。</p>
 * 
 * @author acgist
 */
public final class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
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
			Thread.currentThread().interrupt();
			LOGGER.debug("线程休眠异常", e);
		}
	}
	
	/**
	 * <p>线程休眠</p>
	 * 
	 * @param timeout 休眠时间
	 * @param unit 休眠时间单位
	 */
	public static final void sleep(long timeout, TimeUnit unit) {
		try {
			unit.sleep(timeout);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("线程休眠异常", e);
		}
	}
	
	/**
	 * <p>获取当前用户线程总数</p>
	 * 
	 * @return 当前用户线程总数
	 */
	public static final int activeCount() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		while(threadGroup.getParent() != null) {
			threadGroup = threadGroup.getParent();
		}
		return threadGroup.activeCount();
	}
	
}
