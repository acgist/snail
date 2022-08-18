package com.acgist.snail.utils;

import java.util.Objects;
import java.util.Random;

/**
 * <p>数组工具</p>
 * 
 * @author acgist
 */
public final class ArrayUtils {

	private ArrayUtils() {
	}
	
	/**
	 * <p>异或运算</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 目标数据
	 * 
	 * @return 异或结果
	 */
	public static final byte[] xor(byte[] sources, byte[] targets) {
		Objects.requireNonNull(sources, "异或运算原始参数错误");
		Objects.requireNonNull(targets, "异或运算目标参数错误");
		final int length = sources.length;
		if (length != targets.length) {
			throw new IllegalArgumentException("异或运算参数错误（长度）");
		} else {
			final byte[] value = new byte[length];
			for (int index = 0; index < length; index++) {
				value[index] = (byte) (sources[index] ^ targets[index]);
			}
			return value;
		}
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param objects 数组
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(Object[] objects) {
		return objects == null || objects.length == 0;
	}
	
	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param objects 数组
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(byte[] bytes) {
		return bytes == null || bytes.length == 0;
	}

	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(byte[] bytes) {
		return !isEmpty(bytes);
	}
	
	/**
	 * <p>获取随机字节数组</p>
	 * 
	 * @param length 数组长度
	 * 
	 * @return 字节数组
	 */
	public static final byte[] random(int length) {
		final byte[] bytes = new byte[length];
		final Random random = NumberUtils.random();
		random.nextBytes(bytes);
		return bytes;
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 * 
	 * @see #indexOf(int[], int, int, int)
	 */
	public static final int indexOf(int[] values, int value) {
		return indexOf(values, 0, values.length, value);
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param fromIndex 开始位置
	 * @param toIndex 结束位置
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 */
	public static final int indexOf(int[] values, int fromIndex, int toIndex, int value) {
		toIndex = toIndex > values.length ? values.length : toIndex;
		for (int index = fromIndex; index < toIndex; index++) {
			if(values[index] == value) {
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 * 
	 * @see #indexOf(char[], int, int, char)
	 */
	public static final int indexOf(char[] values, char value) {
		return indexOf(values, 0, values.length, value);
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param fromIndex 开始位置
	 * @param toIndex 结束位置
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 */
	public static final int indexOf(char[] values, int fromIndex, int toIndex, char value) {
		toIndex = toIndex > values.length ? values.length : toIndex;
		for (int index = fromIndex; index < toIndex; index++) {
			if(values[index] == value) {
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * <p>数组反转</p>
	 * 
	 * @param bytes 数组
	 */
	public static final void reverse(byte[] bytes) {
		int index = 0;
		int jndex = bytes.length - 1;
		byte value;
        while (jndex > index) {
        	value = bytes[jndex];
        	bytes[jndex] = bytes[index];
            bytes[index] = value;
            jndex--;
            index++;
        }
	}
	
}
