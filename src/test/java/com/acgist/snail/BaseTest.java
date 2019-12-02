package com.acgist.snail;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.ThreadUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * <p>消耗时间计算</p>
	 */
	protected AtomicLong cos = new AtomicLong();
	
	/**
	 * <p>设置日志级别</p>
	 */
	protected void info() {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLoggerList().forEach(logger -> {
			logger.setLevel(Level.INFO);
		});
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param log 日志信息
	 */
	protected void log(Object log) {
		LOGGER.info("{}", log);
	}
	
	/**
	 * <p>阻止自动关闭</p>
	 */
	protected void pause() {
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	/**
	 * <p>开始计算消耗</p>
	 */
	protected void cost() {
		this.cos.set(System.currentTimeMillis());
	}
	
	protected void costed() {
		final long time = System.currentTimeMillis();
		final long cos = time - this.cos.getAndSet(time);
		this.LOGGER.info("消耗时间：毫秒：{}，秒：{}", cos, cos / 1000);
	}
	
}
