package com.acgist.snail.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;

/**
 * <p>性能分析工具</p>
 * 
 * @author acgist
 */
public abstract class Performance {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Performance.class);
	
	/**
	 * <p>消耗时间统计</p>
	 */
	protected final AtomicLong cost = new AtomicLong();
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 */
	protected final void log(Object message) {
		this.log(null, message);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 * @param args 日志参数
	 */
	protected final void log(String message, Object ... args) {
		if(message == null) {
			message = "{}";
		}
		LOGGER.info(message, args);
	}
	
	/**
	 * <p>统计开始时间</p>
	 */
	protected final void cost() {
		this.cost.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>结束统计时间</p>
	 * <p>重置统计时间</p>
	 */
	protected final void costed() {
		final long time = System.currentTimeMillis();
		final long costed = time - this.cost.getAndSet(time);
		// TODO：多行文本
		LOGGER.info("消耗时间：毫秒：{}，秒：{}", costed, costed / DateUtils.ONE_SECOND);
	}
	
	/**
	 * <p>计算执行消耗</p>
	 * 
	 * @param count 执行次数
	 * @param coster 消耗任务
	 */
	protected final void cost(int count, Coster coster) {
		this.cost();
		for (int index = 0; index < count; index++) {
			coster.execute();
		}
		this.costed();
	}
	
	/**
	 * <p>计算执行消耗</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param coster 消耗任务
	 */
	protected final void cost(int count, int thread, Coster coster) {
		final var latch = new CountDownLatch(count);
		final var executor = Executors.newFixedThreadPool(thread);
		this.cost();
		for (int index = 0; index < count; index++) {
			executor.submit(() -> {
				try {
					coster.execute();
				} catch (Exception e) {
					LOGGER.error("执行异常", e);
				} finally {
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.error("等待异常", e);
			Thread.currentThread().interrupt();
		}
		this.costed();
		SystemThreadContext.shutdownNow(executor);
	}

	/**
	 * <p>线程阻塞</p>
	 */
	public final void pause() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			LOGGER.error("等待异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * <p>消耗任务</p>
	 * 
	 * @author acgist
	 */
	public interface Coster {

		/**
		 * <p>执行任务</p>
		 */
		public void execute();
		
	}
	
}
