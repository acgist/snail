package com.acgist.snail.utils;

import java.util.Map;

/**
 * <p>Map工具</p>
 * 
 * @author acgist
 */
public class MapUtils {

	/**
	 * <p>工具类禁止实例化</p>
	 */
	private MapUtils() {
	}
	
	/**
	 * <p>Map是否为空</p>
	 * 
	 * @param map Map
	 * 
	 * @return true-空；false-非空；
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>Map是否非空</p>
	 * 
	 * @param map Map
	 * 
	 * @return true-非空；false-空；
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
	
}
