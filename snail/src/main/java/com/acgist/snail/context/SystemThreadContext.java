package com.acgist.snail.context;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.TimerException;

/**
 * <p>系统线程上下文</p>
 * <p>所有线程都是守护线程</p>
 * 
 * @author acgist
 */
public final class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	/**
	 * <p>系统线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD = "Snail-Thread";
	/**
	 * <p>BT线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_BT = SNAIL_THREAD + "-BT";
	/**
	 * <p>HLS线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_HLS = SNAIL_THREAD + "-HLS";
	/**
	 * <p>定时线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_TIMER = SNAIL_THREAD + "-Timer";
	/**
	 * <p>BT定时线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_BT_TIMER = SNAIL_THREAD_BT + "-Timer";
	/**
	 * <p>JavaFX平台线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_PLATFORM = SNAIL_THREAD + "-Platform";
	/**
	 * <p>UTP队列线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_UTP_QUEUE = SNAIL_THREAD + "-UTP-Queue";
	/**
	 * <p>下载器线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_DOWNLOADER = SNAIL_THREAD + "-Downloader";
	/**
	 * <p>TCP客户端线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_TCP_CLIENT = SNAIL_THREAD + "-TCP-Client";
	/**
	 * <p>TCP服务端线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_TCP_SERVER = SNAIL_THREAD + "-TCP-Server";
	/**
	 * <p>UDP服务端线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_UDP_SERVER = SNAIL_THREAD + "-UDP-Server";
	/**
	 * <p>HTTP客户端线程：{@value}</p>
	 */
	public static final String SNAIL_THREAD_HTTP_CLIENT = SNAIL_THREAD + "-HTTP-Client";
	
	/**
	 * <p>系统线程池：加快系统运行、防止卡顿</p>
	 */
	private static final ExecutorService EXECUTOR;
	/**
	 * <p>系统定时线程池：定时任务</p>
	 */
	private static final ScheduledExecutorService EXECUTOR_TIMER;
	/**
	 * <p>任务拒绝执行处理</p>
	 */
    public static final RejectedExecutionHandler REJECTED_HANDLER = (runnable, executor) -> LOGGER.error("任务拒绝执行：{}-{}", runnable, executor);
	
	static {
		LOGGER.info("初始化系统线程池");
		EXECUTOR = newExecutor(4, 20, 1000, 60L, SNAIL_THREAD);
		EXECUTOR_TIMER = newTimerExecutor(2, SNAIL_THREAD_TIMER);
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private SystemThreadContext() {
	}
	
	/**
	 * <p>异步任务</p>
	 * 
	 * @param runnable 任务
	 */
	public static final void submit(Runnable runnable) {
		EXECUTOR.submit(runnable);
	}

	/**
	 * <p>定时任务（单次执行）</p>
	 * 
	 * @param delay 延迟时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public static final ScheduledFuture<?> timer(long delay, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		return EXECUTOR_TIMER.schedule(runnable, delay, unit);
	}
	
	/**
	 * <p>定时任务（重复执行）</p>
	 * <p>固定时间（周期不受执行时间影响）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public static final ScheduledFuture<?> timerAtFixedRate(long delay, long period, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		TimerException.verify(period);
		return EXECUTOR_TIMER.scheduleAtFixedRate(runnable, delay, period, unit);
	}
	
	/**
	 * <p>定时任务（重复执行）</p>
	 * <p>固定周期（周期受到执行时间影响）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public static final ScheduledFuture<?> timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		TimerException.verify(period);
		return EXECUTOR_TIMER.scheduleWithFixedDelay(runnable, delay, period, unit);
	}
	
	/**
	 * <p>创建固定线程池</p>
	 * 
	 * @param corePoolSize 初始线程数量
	 * @param maximumPoolSize 最大线程数量
	 * @param queueSize 等待线程队列长度
	 * @param keepAliveTime 线程空闲时间（秒）
	 * @param name 线程池名称
	 * 
	 * @return 线程池
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
	 * 
	 * @param name 线程池名称
	 * 
	 * @return 线程池
	 */
	public static final ExecutorService newCacheExecutor(String name) {
		return new ThreadPoolExecutor(
			0, // 初始线程数量：0
			Integer.MAX_VALUE, // 最大线程数量
			60L, // 线程存活时间：60S
			TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(), // 不限制线程池大小
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * <p>创建定时线程池</p>
	 * 
	 * @param corePoolSize 初始线程数量
	 * @param name 线程池名称
	 * 
	 * @return 定时线程池
	 */
	public static final ScheduledExecutorService newTimerExecutor(int corePoolSize, String name) {
		return new ScheduledThreadPoolExecutor(
			corePoolSize,
			SystemThreadContext.newThreadFactory(name)
		);
	}
	
	/**
	 * <p>创建线程池工厂</p>
	 * 
	 * @param poolName 线程池名称
	 * 
	 * @return 线程池工厂
	 */
	private static final ThreadFactory newThreadFactory(String poolName) {
		return runnable -> {
			final Thread thread = new Thread(runnable);
			thread.setName(poolName);
			thread.setDaemon(true); // 守护线程
			return thread;
		};
	}
	
	/**
	 * <p>关闭系统线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.info("关闭系统线程池");
		shutdown(EXECUTOR);
		shutdown(EXECUTOR_TIMER);
	}
	
	/**
	 * <p>关闭线程池</p>
	 * 
	 * @param executor 线程池
	 */
	public static final void shutdown(ExecutorService executor) {
		shutdown(false, executor);
	}
	
	/**
	 * <p>关闭线程池（立即关闭）</p>
	 * 
	 * @param executor 线程池
	 */
	public static final void shutdownNow(ExecutorService executor) {
		shutdown(true, executor);
	}
	
	/**
	 * <p>关闭线程池</p>
	 * <p>立即关闭：调用正在运行的任务线程的interrupt方法，队列任务不会被执行，不能继续添加任务。</p>
	 * <p>正常关闭：不能继续添加任务，已添加和正在执行的任务都会执行。</p>
	 * 
	 * @param closeNow 是否立即关闭
	 * @param executor 线程池
	 */
	private static final void shutdown(boolean closeNow, ExecutorService executor) {
		if(executor == null || executor.isShutdown()) {
			return;
		}
		try {
			if(closeNow) {
				executor.shutdownNow();
			} else {
				executor.shutdown();
			}
		} catch (Exception e) {
			LOGGER.error("关闭线程池异常", e);
		}
	}
	
	/**
	 * <p>关闭定时任务</p>
	 * 
	 * @param scheduledFuture 定时任务
	 */
	public static final void shutdown(ScheduledFuture<?> scheduledFuture) {
		shutdown(false, scheduledFuture);
	}
	
	/**
	 * <p>关闭定时任务（立即关闭）</p>
	 * 
	 * @param scheduledFuture 定时任务
	 */
	public static final void shutdownNow(ScheduledFuture<?> scheduledFuture) {
		shutdown(true, scheduledFuture);
	}
	
	/**
	 * <p>关闭定时任务</p>
	 * <p>立即关闭：正在运行的任务将被取消执行</p>
	 * <p>正常关闭：正在运行的任务不会取消执行</p>
	 * 
	 * @param closeNow 是否立即关闭
	 * @param scheduledFuture 定时任务
	 */
	private static final void shutdown(boolean closeNow, ScheduledFuture<?> scheduledFuture) {
		if(scheduledFuture == null || scheduledFuture.isCancelled()) {
			return;
		}
		try {
			scheduledFuture.cancel(closeNow);
		} catch (Exception e) {
			LOGGER.error("关闭定时任务异常", e);
		}
	}
	
	/**
	 * <p>关闭异步通道线程池</p>
	 * 
	 * @param group 异步通道线程池
	 */
	public static final void shutdown(AsynchronousChannelGroup group) {
		shutdown(false, group);
	}
	
	/**
	 * <p>关闭异步通道线程池（立即关闭）</p>
	 * 
	 * @param group 异步通道线程池
	 */
	public static final void shutdownNow(AsynchronousChannelGroup group) {
		shutdown(true, group);
	}
	
	/**
	 * <p>关闭异步通道线程池</p>
	 * 
	 * @param closeNow 是否立即关闭
	 * @param group 异步通道线程池
	 */
	private static final void shutdown(boolean closeNow, AsynchronousChannelGroup group) {
		if(group == null || group.isShutdown()) {
			return;
		}
		try {
			if(closeNow) {
				group.shutdownNow();
			} else {
				group.shutdown();
			}
		} catch (Exception e) {
			LOGGER.error("关闭异步通道线程池异常", e);
		}
	}

}
