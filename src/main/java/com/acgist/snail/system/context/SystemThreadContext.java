package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * 系统线程
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	public static final String SNAIL_THREAD = "Snail-Thread";
	public static final String SNAIL_THREAD_BT = SNAIL_THREAD + "-BT";
	public static final String SNAIL_THREAD_HTTP = SNAIL_THREAD + "-HTTP";
	public static final String SNAIL_THREAD_TIMER = SNAIL_THREAD + "-Timer";
	public static final String SNAIL_THREAD_BT_TIMER = SNAIL_THREAD + "-BT-Timer";
	public static final String SNAIL_THREAD_PLATFORM = SNAIL_THREAD + "-Platform";
	public static final String SNAIL_THREAD_DOWNLOADER = SNAIL_THREAD + "-Downloader";
	public static final String SNAIL_THREAD_TCP_CLIENT = SNAIL_THREAD + "-TCP-Client";
	public static final String SNAIL_THREAD_TCP_SERVER = SNAIL_THREAD + "-TCP-Server";
	public static final String SNAIL_THREAD_UDP_CLIENT = SNAIL_THREAD + "-UDP-Client";
	
	/**
	 * 线程池：大小限制，主要用来处理一些不是非常急用的任务
	 */
	private static final ExecutorService EXECUTOR;
	/**
	 * 定时线程池：定时任务
	 */
	private static final ScheduledExecutorService EXECUTOR_TIMER;
	
	static {
		LOGGER.info("启动系统线程池");
		EXECUTOR = newExecutor(10, 100, 10, 60L, SNAIL_THREAD);
		EXECUTOR_TIMER = newScheduledExecutor(4, SNAIL_THREAD_TIMER);
	}
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void submit(Runnable runnable) {
		EXECUTOR.submit(runnable);
	}
	
	/**
	 * 定时任务（不重复）
	 */
	public static final ScheduledFuture<?> timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return EXECUTOR_TIMER.schedule(runnable, delay, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * 定时任务（重复），固定时间（周期不受执行时间影响）
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public static final ScheduledFuture<?> timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return EXECUTOR_TIMER.scheduleAtFixedRate(runnable, delay, period, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * 定时任务（重复），固定周期（周期受到执行时间影响）
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public static final ScheduledFuture<?> timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return EXECUTOR_TIMER.scheduleWithFixedDelay(runnable, delay, period, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * 线程池
	 */
	public static final ExecutorService newExecutor(int corePoolSize, int maximumPoolSize, int queueSize, long keepAliveTime, String name) {
		return new ThreadPoolExecutor(
			corePoolSize, // 初始线程数量
			maximumPoolSize, // 最大线程数量
			keepAliveTime, // 空闲时间
			TimeUnit.SECONDS, // 空闲时间单位
			new LinkedBlockingQueue<Runnable>(queueSize), // 等待线程
			SystemThreadContext.newThreadFactory(name) // 线程工厂
		);
	}
	
	/**
	 * 无限线程池
	 */
	public static final ExecutorService newCacheExecutor(String name) {
		return Executors.newCachedThreadPool(SystemThreadContext.newThreadFactory(name));
	}
	
	/**
	 * 定时线程池
	 */
	public static final ScheduledExecutorService newScheduledExecutor(int corePoolSize, String name) {
		return Executors.newScheduledThreadPool(
			corePoolSize,
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * 线程池工厂
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
		shutdown(EXECUTOR_TIMER);
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown(ExecutorService executor) {
		if(executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdownNow(ExecutorService executor) {
		if(executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
		}
	}
	
}
