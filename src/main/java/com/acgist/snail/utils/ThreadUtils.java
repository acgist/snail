package com.acgist.snail.utils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.function.ConditionFunction;

/**
 * utils - 线程
 */
public class ThreadUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * 休眠
	 */
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("线程休眠异常");
		}
	}
	
	/**
	 * 超时方法：<br>
	 * 跳出死循环条件：<br>
	 * 1.满足跳出条件<br>
	 * 2.超过超时时间
	 * @param timeout 超时时间（毫秒）
	 */
	public static final void timeout(long timeout, ConditionFunction function) {
		try {
			CompletableFuture.runAsync(() -> {
				while(true) {
					if(function.condition()) {
						break;
					}
//					Thread.yield(); // 使用休眠占用资源较少
					ThreadUtils.sleep(100);
				}
			}).get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("线程超时", e);
		}
	}

	/**
	 * 线程等待
	 * wait会让出CPU执行其他的任务，线程池中同样会让出线程
	 */
	public static final void wait(Object obj, Duration timeout) {
		try {
			obj.wait(timeout.toMillis());
		} catch (InterruptedException e) {
			LOGGER.error("线程等待异常", e);
		}
	}
	
}
