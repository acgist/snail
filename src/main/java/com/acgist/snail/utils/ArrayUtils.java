package com.acgist.snail.utils;

/**
 * <p>数组工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ArrayUtils {

	/**
	 * 比较：如果数组元素全部一致则相等
	 * 
	 * @param sources 原数据
	 * @param targets 比较数据
	 */
	public static final boolean equals(byte[] sources, byte[] targets) {
		if(sources == targets) {
			return true;
		}
		if(sources == null || targets == null) {
			return false;
		}
		if(sources.length != targets.length) {
			return false;
		}
		for (int index = 0; index < sources.length; index++) {
			if(sources[index] != targets[index]) {
				return false;
			}
		}
		return true;
	}
	
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
	 * <p>判断数组是否为空</p>
	 * <p>数组==null或者长度==0时返回true，反之返回false。</p>
	 */
	public static boolean isEmpty(byte[] value) {
		return value == null || value.length == 0;
	}

	/**
	 * 判断数组是否不为空
	 */
	public static boolean isNotEmpty(byte[] value) {
		return !isEmpty(value);
	}
	
}
