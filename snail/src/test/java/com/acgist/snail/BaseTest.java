package com.acgist.snail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.ThreadUtils;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * <p>消耗时间标记</p>
	 */
	protected AtomicLong cos = new AtomicLong();
	
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
	
	/**
	 * <p>计算消耗（多次执行）</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param function 任务
	 */
	protected void cost(int count, int thread, Consumer<Void> function) {
		this.cost();
		final CountDownLatch latch = new CountDownLatch(count);
		final var executor = Executors.newFixedThreadPool(thread);
		for (int index = 0; index < count; index++) {
			executor.submit(() -> {
				try {
					function.accept(null);
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
	
}
