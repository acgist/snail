package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统线程
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	private static final ExecutorService EXECUTOR;
	
	static {
		LOGGER.info("启动系统线程");
		EXECUTOR = Executors.newCachedThreadPool(newThreadFactory("System Thread"));
	}
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void runasyn(Runnable runnable) {
		EXECUTOR.submit(runnable);
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
