package com.acgist.snail.utils;

/**
 * 数组集合工具
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
	
}
