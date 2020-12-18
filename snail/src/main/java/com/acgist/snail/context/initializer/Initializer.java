package com.acgist.snail.context.initializer;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;

/**
 * <p>初始化</p>
 * 
 * @author acgist
 */
public abstract class Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
	
	/**
	 * <p>初始化延迟时间（单位：秒）</p>
	 * 
	 * @see #asyn()
	 */
	private final int delay;

	protected Initializer() {
		this(0);
	}
	
	/**
	 * @param delay 延迟时间（单位：秒）
	 */
	protected Initializer(int delay) {
		this.delay = delay;
	}

	/**
	 * <p>同步初始化</p>
	 */
	public final void sync() {
		try {
			this.init();
		} catch (NetException | DownloadException e) {
			LOGGER.error("同步初始化异常", e);
		}
	}
	
	/**
	 * <p>异步初始化</p>
	 */
	public final void asyn() {
		// 异步任务
		final Runnable runnable = () -> {
			try {
				this.init();
			} catch (NetException | DownloadException e) {
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
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void init() throws NetException, DownloadException;
	
}
