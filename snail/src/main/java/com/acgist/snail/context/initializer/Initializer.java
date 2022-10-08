package com.acgist.snail.context.initializer;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>初始化器</p>
 * 
 * @author acgist
 */
public abstract class Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
	
	/**
	 * <p>名称</p>
	 */
	private final String name;
	/**
	 * <p>延迟时间（秒）</p>
	 * 
	 * @see #asyn()
	 */
	private final int delay;

	/**
	 * <p>初始化器</p>
	 * 
	 * @param name 名称
	 */
	protected Initializer(String name) {
		this(name, 0);
	}
	
	/**
	 * <p>初始化器</p>
	 * 
	 * @param name 名称
	 * @param delay 延迟时间（秒）
	 */
	protected Initializer(String name, int delay) {
		this.name = name;
		this.delay = delay;
	}

	/**
	 * <p>同步执行初始方法</p>
	 */
	public final void sync() {
		try {
			LOGGER.debug("同步执行初始方法：{}", this.name);
			this.init();
		} catch (NetException | DownloadException e) {
			LOGGER.error("同步执行初始方法异常：{}", this.name, e);
		}
	}
	
	/**
	 * <p>异步执行初始方法</p>
	 */
	public final void asyn() {
		final Runnable runnable = () -> {
			try {
				LOGGER.debug("异步执行初始方法：{}", this.name);
				this.init();
			} catch (NetException | DownloadException e) {
				LOGGER.error("异步执行初始方法异常：{}", this.name, e);
			}
		};
		if(this.delay <= 0) {
			SystemThreadContext.submit(runnable);
		} else {
			SystemThreadContext.scheduled(
				this.delay,
				TimeUnit.SECONDS,
				runnable
			);
		}
	}
	
	/**
	 * <p>初始方法</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void init() throws NetException, DownloadException;
	
}
