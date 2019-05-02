package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean工具
 */
public class BeanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);
	
	/**
	 * 调用默认构造方法（无参）生成实例
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("通过反射获取实例异常", e);
		}
		return null;
	}
	
}
