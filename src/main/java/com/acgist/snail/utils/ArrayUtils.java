package com.acgist.snail.utils;

public class ArrayUtils {

	/**
	 * 比较：如果数组元素全部一致则相等
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
	
	public static boolean isEmpty(byte[] value) {
		return value == null || value.length == 0;
	}

	public static boolean isNotEmpty(byte[] value) {
		return !isEmpty(value);
	}
	
}
