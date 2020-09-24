package com.acgist.snail.context.initializer;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;

/**
 * <p>初始化</p>
 * <p>设置{@code delay}后将延迟启动</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
	
	/**
	 * <p>延迟启动</p>
	 * <p>单位：秒</p>
	 */
	private final int delay;

	/**
	 * <p>立即初始化</p>
	 */
	protected Initializer() {
		this(0);
	}
	
	/**
	 * <p>延迟初始化</p>
	 * 
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
		// 任务
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
