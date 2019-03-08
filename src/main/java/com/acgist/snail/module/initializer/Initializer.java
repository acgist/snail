package com.acgist.snail.module.initializer;

import com.acgist.snail.context.SystemThreadContext;

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
		SystemThreadContext.runasyn("Asyn Initializer Thread", () -> {
			this.init();
		});
	}
	
	/**
	 * 初始化
	 */
	protected abstract void init();
	
}
