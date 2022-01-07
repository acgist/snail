package com.acgist.snail.utils;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>IO工具</p>
 * 
 * @author acgist
 */
public final class IoUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);
	
	private IoUtils() {
	}
	
	/**
	 * <p>关闭Closeable</p>
	 * 
	 * @param closeable Closeable
	 */
	public static final void close(AutoCloseable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭Closeable异常", e);
		}
	}
	
}
