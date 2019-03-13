package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统线程
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	private static final int TIMER_EXECUTOR_SIZE = 2; // 定时线程池
	
	/**
	 * 线程池
	 */
	private static final ExecutorService EXECUTOR;
	/**
	 * 定时线程池
	 */
	private static final ScheduledExecutorService TIMER_EXECUTOR;
	
	static {
		LOGGER.info("启动系统线程池");
		EXECUTOR = Executors.newCachedThreadPool(newThreadFactory("System Thread"));
		TIMER_EXECUTOR = Executors.newScheduledThreadPool(TIMER_EXECUTOR_SIZE, SystemThreadContext.newThreadFactory("System Timer Thread"));
	}
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void runasyn(Runnable runnable) {
		EXECUTOR.submit(runnable);
	}
	
	/**
	 * 定时任务
	 */
	public static final void timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		TIMER_EXECUTOR.scheduleAtFixedRate(runnable, delay, period, unit);
	}
	
	/**
	 * 新建线程池工厂
	 * @param poolName 线程池名称
	 */
	public static final ThreadFactory newThreadFactory(String poolName) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName(poolName);
				thread.setDaemon(true);
				return thread;
			}
		};
	}
	
	/**
	 * 关闭线程
	 */
	public static final void shutdown() {
		LOGGER.info("关闭系统线程池");
		shutdown(EXECUTOR);
		shutdown(TIMER_EXECUTOR);
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown(ExecutorService executor) {
		if(executor != null && !executor.isShutdown()) {
			executor.shutdown();
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.error("线程池关闭等待异常", e);
			}
		}
	}
	
}
