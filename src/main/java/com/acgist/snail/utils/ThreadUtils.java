package com.acgist.snail.utils;

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
		long start = System.currentTimeMillis();
		while(true) {
			if(function.condition()) {
				break;
			}
//			Thread.yield(); // 使用休眠占用资源较少
			ThreadUtils.sleep(10);
			if(System.currentTimeMillis() - start > timeout) {
				break;
			}
		}
	}

}
