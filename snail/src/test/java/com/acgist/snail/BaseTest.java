package com.acgist.snail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * <p>消耗时间标记</p>
	 */
	protected AtomicLong cost = new AtomicLong();
	
	public void pause() {
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param obj 日志信息
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
		this.cost.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>计算消耗并重置标记</p>
	 */
	protected void costed() {
		final long time = System.currentTimeMillis();
		final long cos = time - this.cost.getAndSet(time);
		// TODO：多行文本
		this.LOGGER.info("消耗时间：毫秒：{}，秒：{}", cos, cos / 1000);
	}
	
	/**
	 * <p>计算消耗（多次执行）</p>
	 * 
	 * @param count 执行次数
	 * @param function 消耗任务
	 */
	protected void cost(int count, Coster function) {
		this.cost();
		for (int index = 0; index < count; index++) {
			function.execute();
		}
		this.costed();
	}
	
	/**
	 * <p>计算消耗（多次执行）</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param function 消耗任务
	 */
	protected void cost(int count, int thread, Coster function) {
		final var latch = new CountDownLatch(count);
		final var executor = Executors.newFixedThreadPool(thread);
		this.cost();
		for (int index = 0; index < count; index++) {
			executor.submit(() -> {
				try {
					function.execute();
				} catch (Exception e) {
					this.LOGGER.error("执行异常", e);
				} finally {
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			this.LOGGER.debug("线程等待异常", e);
			Thread.currentThread().interrupt();
		}
		this.costed();
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
