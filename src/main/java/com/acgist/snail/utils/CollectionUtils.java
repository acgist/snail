package com.acgist.snail.utils;

import java.util.Collection;
import java.util.Map;

/**
 * <p>集合、Map工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class CollectionUtils {

	/**
	 * 空集合：集合==null或者长度==0
	 */
	public static final boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * 非空集合
	 */
	public static final boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

	/**
	 * 空Map：Map==null或者长度==0
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * 非空Map
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

}
