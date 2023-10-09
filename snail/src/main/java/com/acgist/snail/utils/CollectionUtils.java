package com.acgist.snail.utils;

import java.util.Collection;
import java.util.List;

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
		return !CollectionUtils.isEmpty(list);
	}
	
	/**
	 * 获取集合首个元素
	 * 
	 * @param <T> 泛型
	 * 
	 * @param list 集合
	 * 
	 * @return 首个元素
	 */
	public static final <T> T getFirst(List<T> list) {
		return (list == null || list.isEmpty()) ? null : list.get(0);
	}

	/**
	 * 获取集合尾部元素
	 * 
	 * @param <T> 泛型
	 * 
	 * @param list 集合
	 * 
	 * @return 尾部元素
	 */
	public static final <T> T getLast(List<T> list) {
		return (list == null || list.isEmpty()) ? null : list.get(list.size() - 1);
	}
	
}
