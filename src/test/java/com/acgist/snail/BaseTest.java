package com.acgist.snail;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * <p>消耗时间计算</p>
	 */
	protected AtomicLong cos = new AtomicLong();
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param log 日志信息
	 */
	protected void log(Object log) {
		LOGGER.debug("{}", log);
	}
	
	/**
	 * <p>阻止自动关闭</p>
	 */
	protected void pause() {
		this.pause();
	}
	
	/**
	 * <p>开始计算消耗</p>
	 */
	protected void cost() {
		this.cos.set(System.currentTimeMillis());
	}
	
	protected void costed() {
		final long cos = System.currentTimeMillis() - this.cos.get();
		this.LOGGER.debug("消耗时间：毫秒：{}，秒：{}", cos, cos / 1000);
	}
	
}
