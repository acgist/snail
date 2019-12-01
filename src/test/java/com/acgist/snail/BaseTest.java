package com.acgist.snail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
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
	
}
