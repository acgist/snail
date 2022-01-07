package com.acgist.snail.utils;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>线程工具</p>
 * 
 * @author acgist
 */
public final class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
	private ThreadUtils() {
	}
	
	/**
	 * <p>线程休眠</p>
	 * <p>注意：sleep不释放锁，wait会释放锁。</p>
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
