package com.acgist.snail.module.initializer;

/**
 * 初始化
 */
public abstract class Initializer {

	/**
	 * 同步执行
	 */
	public void initSync() {
		this.init();
	}
	
	/**
	 * 异步执行
	 */
	public void initAsyn() {
		Thread thread = new Thread(() -> {
			this.init();
		});
		thread.setName("Asyn Initializer");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * 初始化
	 */
	protected abstract void init();
	
}
