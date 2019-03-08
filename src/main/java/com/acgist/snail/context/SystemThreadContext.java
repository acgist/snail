package com.acgist.snail.context;

/**
 * 异步执行
 */
public class SystemThreadContext {

	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final Thread runasyn(String name, Runnable runnable) {
		Thread thread = thread(name, runnable);
		thread.run();
		return thread;
	}
	
	/**
	 * 获取线程，但不自动运行
	 */
	public static final Thread thread(String name, Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setName(name);
		thread.setDaemon(true);
		return thread;
	}
	
}
