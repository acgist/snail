package com.acgist.snail.utils;

import com.acgist.snail.system.exception.ArgumentException;

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
	 * <p>比较字符数组大小（无符号比较）</p>
	 * <p>长度不同时：长度长的数组大。</p>
	 * <p>长度相同时：比较每一个字符，高位字符大的大。</p>
	 * 
	 * @since 1.1.0
	 */
	public static final int compareUnsigned(byte[] sources, byte[] targets) {
		if(sources == null || targets == null) {
			throw new ArgumentException("数组比较时参数不正确。");
		} else if(sources.length != targets.length) {
			return sources.length > targets.length ? 1 : -1;
		} else {
			for (int index = 0; index < sources.length; index++) {
				if(sources[index] != targets[index]) {
					return ((char) sources[index]) > ((char) targets[index]) ? 1 : -1;
				}
			}
			return 0;
		}
	}
	
	public static void main(String[] args) {
		byte i = -2;
		System.out.println(i);
		System.out.println((char) i);
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
