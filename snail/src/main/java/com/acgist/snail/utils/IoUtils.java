package com.acgist.snail.utils;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IO工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class IoUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private IoUtils() {
	}
	
	/**
	 * <p>关闭{@code Closeable}</p>
	 * 
	 * @param closeable {@code Closeable}
	 */
	public static final void close(Closeable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭Closeable异常", e);
		}
	}
	
}
