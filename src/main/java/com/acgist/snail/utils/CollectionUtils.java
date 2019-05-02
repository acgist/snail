package com.acgist.snail.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 数组、集合、Map工具
 */
public class CollectionUtils {

	/**
	 * 空数组
	 */
	public static final boolean isEmpty(Object[] objects) {
		return objects == null || objects.length == 0;
	}
	
	/**
	 * 非空数组
	 */
	public static final boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}
	
	/**
	 * 空集合
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
	 * 空Map
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
