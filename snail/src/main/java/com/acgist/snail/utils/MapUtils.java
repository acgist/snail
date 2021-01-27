package com.acgist.snail.utils;

import java.util.Map;

/**
 * <p>Map工具</p>
 * 
 * @author acgist
 */
public class MapUtils {

	private MapUtils() {
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
	
}
