package com.acgist.snail.utils;

import java.util.List;

/**
 * utils - 数组、集合
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
	
	public static final boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}
	
	public static final boolean isNotEmpty(List<?> list) {
		return !isEmpty(list);
	}
	
}
