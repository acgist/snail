package com.acgist.snail.context.initializer;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;

/**
 * <p>初始化</p>
 * 
 * @author acgist
 */
public abstract class Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
	
	/**
	 * <p>延迟启动</p>
	 * <p>单位：秒</p>
	 * 
	 * @see #asyn()
	 */
	private final int delay;

	protected Initializer() {
		this(0);
	}
	
	/**
	 * @param delay 延迟时间
	 */
	protected Initializer(int delay) {
		this.delay = delay;
	}

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
		// 异步任务
		final Runnable runnable = () -> {
			try {
				this.init();
			} catch (Exception e) {
				LOGGER.error("异步初始化异常", e);
			}
		};
		if(this.delay <= 0) {
			// 立即初始化
			SystemThreadContext.submit(runnable);
		} else {
			// 延迟初始化
			SystemThreadContext.timer(
				this.delay,
				TimeUnit.SECONDS,
				runnable
			);
		}
	}
	
	/**
	 * <p>初始化</p>
	 * 
	 * @throws Exception 初始化异常
	 */
	protected abstract void init() throws Exception;
	
}
