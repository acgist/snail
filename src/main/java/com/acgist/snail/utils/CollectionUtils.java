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
	 * <p>集合是否为空</p>
	 */
	public static final boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}
	
	/**
	 * <p>集合是否包含数据</p>
	 */
	public static final boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

	/**
	 * <p>Map是否为空</p>
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>Map是否包含数据</p>
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

}
