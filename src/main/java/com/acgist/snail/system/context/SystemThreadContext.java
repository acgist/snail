package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>系统线程上下文</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	/** 系统线程 */
	public static final String SNAIL_THREAD = "Snail-Thread";
	/** BT线程 */
	public static final String SNAIL_THREAD_BT = SNAIL_THREAD + "-BT";
	/** HTTP线程 */
	public static final String SNAIL_THREAD_HTTP = SNAIL_THREAD + "-HTTP";
	/** 定时线程 */
	public static final String SNAIL_THREAD_TIMER = SNAIL_THREAD + "-Timer";
	/** BT定时线程 */
	public static final String SNAIL_THREAD_BT_TIMER = SNAIL_THREAD + "-BT-Timer";
	/** JavaFX平台线程 */
	public static final String SNAIL_THREAD_PLATFORM = SNAIL_THREAD + "-Platform";
	/** 下载器线程 */
	public static final String SNAIL_THREAD_DOWNLOADER = SNAIL_THREAD + "-Downloader";
	/** TCP客户端线程 */
	public static final String SNAIL_THREAD_TCP_CLIENT = SNAIL_THREAD + "-TCP-Client";
	/** TCP服务端线程 */
	public static final String SNAIL_THREAD_TCP_SERVER = SNAIL_THREAD + "-TCP-Server";
	/** UDP服务端线程 */
	public static final String SNAIL_THREAD_UDP_SERVER = SNAIL_THREAD + "-UDP-Server";
	/** UDP处理器线程 */
	public static final String SNAIL_THREAD_UDP_HANDLER = SNAIL_THREAD + "-UDP-Handler";
	
	/**
	 * 系统线程池：加快系统运行、防止卡顿。例如：初始化、关闭资源、文件校验等。
	 */
	private static final ExecutorService EXECUTOR;
	/**
	 * 系统定时线程池：定时任务
	 */
	private static final ScheduledExecutorService EXECUTOR_TIMER;
	
	static {
		LOGGER.info("启动系统线程池");
		EXECUTOR = newExecutor(4, 20, 100, 60L, SNAIL_THREAD);
		EXECUTOR_TIMER = newScheduledExecutor(2, SNAIL_THREAD_TIMER);
	}
	
	/**
	 * <p>异步执行</p>
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
	 * <p>定时任务（重复）</p>
	 * <p>固定时间（周期不受执行时间影响）</p>
	 * 
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
	 * <p>定时任务（重复）</p>
	 * <p>固定周期（周期受到执行时间影响）</p>
	 * 
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
	 * 创建线程池
	 * 
	 * @param corePoolSize 初始线程数量
	 * @param maximumPoolSize 最大线程数量
	 * @param queueSize 等待线程队列长度
	 * @param keepAliveTime 线程空闲时间（秒）
	 * @param name 线程名称
	 */
	public static final ExecutorService newExecutor(int corePoolSize, int maximumPoolSize, int queueSize, long keepAliveTime, String name) {
		return new ThreadPoolExecutor(
			corePoolSize,
			maximumPoolSize,
			keepAliveTime,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(queueSize),
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * <p>创建缓存线程池</p>
	 * <p>不限制线程池大小，初始线程：0，存活时间：60S。</p>
	 * 
	 * @param name 线程池名称
	 */
	public static final ExecutorService newCacheExecutor(String name) {
		return new ThreadPoolExecutor(
			0,
			Integer.MAX_VALUE,
			60L,
			TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(),
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * 创建定时线程池
	 * 
	 * @param corePoolSize 初始线程数量
	 * @param name 线程池名称
	 */
	public static final ScheduledExecutorService newScheduledExecutor(int corePoolSize, String name) {
		return new ScheduledThreadPoolExecutor(
			corePoolSize,
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * 创建线程池工厂
	 * 
	 * @param poolName 线程池名称
	 */
	private static final ThreadFactory newThreadFactory(String poolName) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				final Thread thread = new Thread(runnable);
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
		if(executor == null || executor.isShutdown()) {
			return;
		}
		try {
			executor.shutdown();
		} catch (Exception e) {
			LOGGER.error("关闭线程池异常", e);
		}
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdownNow(ExecutorService executor) {
		if(executor == null || executor.isShutdown()) {
			return;
		}
		try {
			executor.shutdownNow();
		} catch (Exception e) {
			LOGGER.error("关闭线程池异常", e);
		}
	}
	
	/**
	 * 关闭定时任务
	 * 
	 * @param scheduledFuture 定时任务
	 * 
	 * @since 1.1.0
	 */
	public static final void shutdown(ScheduledFuture<?> scheduledFuture) {
		if(scheduledFuture == null || scheduledFuture.isCancelled()) {
			return;
		}
		try {
			scheduledFuture.cancel(true);
		} catch (Exception e) {
			LOGGER.error("定时任务取消异常", e);
		}
	}

}
