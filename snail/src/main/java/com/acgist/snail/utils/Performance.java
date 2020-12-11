package com.acgist.snail.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;

/**
 * <p>性能分析工具</p>
 * 
 * @author acgist
 */
public abstract class Performance {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Performance.class);
	
	/**
	 * <p>是否跳过消耗测试：{@value}</p>
	 */
	private static final String COST_SKIP = "skip";
	/**
	 * <p>是否跳过消耗测试：{@value}</p>
	 */
	private static final String COST_FALSE = "false";
	
	/**
	 * <p>消耗时间统计</p>
	 */
	protected final AtomicLong costTime = new AtomicLong();
	
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
	 * <p>判断是否跳过消耗测试</p>
	 * 
	 * @return 是否跳过消耗测试
	 */
	protected final boolean skipCost() {
		final String cost = System.getProperty("cost");
		if(COST_SKIP.equalsIgnoreCase(cost) || COST_FALSE.equals(cost)) {
			return true;
		}
		return false;
	}
	
	/**
	 * <p>统计开始时间</p>
	 */
	protected final void cost() {
		this.costTime.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>结束统计消耗时间</p>
	 * <p>重置消耗时间统计</p>
	 * 
	 * @return 消耗时间
	 */
	protected final long costed() {
		final long time = System.currentTimeMillis();
		final long costed = time - this.costTime.getAndSet(time);
		// TODO：多行文本
		LOGGER.info("消耗时间（毫秒）：{}", costed);
		LOGGER.info("消耗时间（秒）：{}", costed / SystemConfig.ONE_SECOND_MILLIS);
		return costed;
	}
	
	/**
	 * <p>计算执行消耗时间</p>
	 * 
	 * @param count 执行次数
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, Coster coster) {
		if(this.skipCost()) {
			this.log("跳过消耗测试");
			return 0L;
		}
		this.cost();
		for (int index = 0; index < count; index++) {
			coster.execute();
		}
		return this.costed();
	}
	
	/**
	 * <p>计算执行消耗时间</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, int thread, Coster coster) {
		if(this.skipCost()) {
			this.log("跳过消耗测试");
			return 0L;
		}
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
		final long costed = this.costed();
		SystemThreadContext.shutdownNow(executor);
		return costed;
	}

	/**
	 * <p>线程阻塞</p>
	 */
	public final void pause() {
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				LOGGER.error("等待异常", e);
				Thread.currentThread().interrupt();
			}
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
