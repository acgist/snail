package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统线程
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	public static final String SNAIL_THREAD = "Snail-Thread";
	public static final String SNAIL_THREAD_HTTP = SNAIL_THREAD + "-HTTP";
	public static final String SNAIL_THREAD_TIMER = SNAIL_THREAD + "-Timer";
	public static final String SNAIL_THREAD_TRACKER = SNAIL_THREAD + "-Tracker";
	
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
		EXECUTOR = newExecutor(4, 100, 60L, SNAIL_THREAD);
		TIMER_EXECUTOR = newScheduledExecutor(2, SNAIL_THREAD_TIMER);
	}
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void submit(Runnable runnable) {
		EXECUTOR.submit(runnable);
	}
	
	/**
	 * 定时任务（重复）
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public static final void timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			TIMER_EXECUTOR.scheduleAtFixedRate(runnable, delay, period, unit);
		}
	}

	/**
	 * 定时任务（不重复）
	 */
	public static final void timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			TIMER_EXECUTOR.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * 获取系统线程池
	 */
	public static final ExecutorService systemExecutor() {
		return EXECUTOR;
	}
	
	/**
	 * 新建线程池
	 */
	public static final ExecutorService newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, String name) {
		return new ThreadPoolExecutor(
			corePoolSize, // 初始线程数量
			maximumPoolSize, // 最大线程数量
			keepAliveTime, // 空闲时间
			TimeUnit.SECONDS, // 空闲时间单位
			new LinkedBlockingQueue<Runnable>(), // 等待线程
			SystemThreadContext.newThreadFactory(name) // 线程工厂
		);
	}
	
	/**
	 * 新建定时线程池
	 */
	public static final ScheduledExecutorService newScheduledExecutor(int corePoolSize, String name) {
		return Executors.newScheduledThreadPool(
			corePoolSize,
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * 新建线程池工厂
	 * @param poolName 线程池名称
	 */
	private static final ThreadFactory newThreadFactory(String poolName) {
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
		}
	}
	
}
