package com.acgist.snail.utils;

import java.util.Collection;

/**
 * <p>集合工具</p>
 * 
 * @author acgist
 */
public final class CollectionUtils {

	/**
	 * <p>工具类禁止实例化</p>
	 */
	private CollectionUtils() {
	}
	
	/**
	 * <p>集合是否为空</p>
	 * 
	 * @param list 集合
	 * 
	 * @return true-空；false-非空；
	 */
	public static final boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * <p>集合是否非空</p>
	 * 
	 * @param list 集合
	 * 
	 * @return true-非空；false-空；
	 */
	public static final boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

}
