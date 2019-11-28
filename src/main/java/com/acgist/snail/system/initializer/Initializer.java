package com.acgist.snail.system.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>初始化</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
	
	/**
	 * <p>同步初始化</p>
	 */
	public void sync() {
		try {
			this.init();
		} catch (Exception e) {
			LOGGER.error("同步初始化异常", e);
		}
	}
	
	/**
	 * <p>异步初始化</p>
	 */
	public void asyn() {
		SystemThreadContext.submit(() -> {
			try {
				this.init();
			} catch (Exception e) {
				LOGGER.error("异步初始化异常", e);
			}
		});
	}
	
	/**
	 * <p>初始化</p>
	 * 
	 * @throws Exception 初始化异常
	 */
	protected abstract void init() throws Exception;
	
}
