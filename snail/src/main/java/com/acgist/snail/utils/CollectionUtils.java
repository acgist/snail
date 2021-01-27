package com.acgist.snail.utils;

import java.util.Collection;

/**
 * <p>集合工具</p>
 * 
 * @author acgist
 */
public final class CollectionUtils {

	private CollectionUtils() {
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param list 集合
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param list 集合
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

}
