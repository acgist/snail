package com.acgist.snail.context;

/**
 * 异步执行
 */
public class SystemAsynContext {

	/**
	 * 异步执行：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void runasyn(String name, Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
	}
	
}
