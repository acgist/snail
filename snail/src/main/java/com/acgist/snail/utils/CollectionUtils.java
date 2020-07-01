package com.acgist.snail.utils;

import java.util.Collection;
import java.util.Map;

/**
 * <p>集合、Map工具</p>
 * 
 * @author acgist
 * @since 1.0.0
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
	 * @return {@code true}-空；{@code false}-非空；
	 */
	public static final boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * <p>集合是否非空</p>
	 * 
	 * @param list 集合
	 * 
	 * @return {@code true}-非空；{@code false}-空；
	 */
	public static final boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

	/**
	 * <p>{@code Map}是否为空</p>
	 * 
	 * @param map {@code Map}
	 * 
	 * @return {@code true}-空；{@code false}-非空；
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>{@code Map}是否非空</p>
	 * 
	 * @param map {@code Map}
	 * 
	 * @return {@code true}-非空；{@code false}-空；
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

}
