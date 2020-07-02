package com.acgist.snail.javafx;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.ThreadUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * <p>消耗时间标记</p>
	 */
	protected AtomicLong cos = new AtomicLong();
	
	/**
	 * <p>设置日志级别：INFO</p>
	 */
	protected void info() {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLoggerList().forEach(logger -> {
			logger.setLevel(Level.INFO);
		});
	}
	
	/**
	 * <p>阻止自动关闭</p>
	 */
	protected void pause() {
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param obj 日志对象
	 */
	protected void log(Object obj) {
		this.log(null, obj);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 * @param args 日志参数
	 */
	protected void log(String message, Object ... args) {
		if(message == null) {
			message = "{}";
		}
		this.LOGGER.info(message, args);
	}
	
	/**
	 * <p>开始计算消耗</p>
	 */
	protected void cost() {
		this.cos.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>计算消耗并重置标记</p>
	 */
	protected void costed() {
		final long time = System.currentTimeMillis();
		final long cos = time - this.cos.getAndSet(time);
		// TODO：多行文本
		this.LOGGER.info("消耗时间：毫秒：{}，秒：{}", cos, cos / 1000);
	}
	
}
