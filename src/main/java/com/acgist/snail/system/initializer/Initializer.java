package com.acgist.snail.system.initializer;

import com.acgist.snail.system.context.SystemThreadContext;

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
		SystemThreadContext.submit(() -> {
			this.init();
		});
	}
	
	/**
	 * 初始化
	 */
	protected abstract void init();
	
}
