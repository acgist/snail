package com.acgist.snail.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 异步执行
 */
public class SystemThreadContext {

	private static final ExecutorService SYSTEM_EXECUTOR = Executors.newCachedThreadPool(newThreadFactory("System Thread"));
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void runasyn(Runnable runnable) {
		SYSTEM_EXECUTOR.submit(runnable);
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
		SYSTEM_EXECUTOR.shutdown();
	}
	
}
