package com.acgist.snail.utils;

import java.util.Objects;
import java.util.Random;

import com.acgist.snail.config.SystemConfig;

/**
 * <p>数组工具</p>
 * 
 * @author acgist
 */
public final class ArrayUtils {

	/**
	 * <p>工具类禁止实例化</p>
	 */
	private ArrayUtils() {
	}
	
	/**
	 * <p>查找数组索引没有匹配索引：{@value}</p>
	 */
	public static final int NONE_INDEX = -1;
	
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
			final byte[] result = new byte[length];
			for (int index = 0; index < length; index++) {
				result[index] = (byte) (sources[index] ^ targets[index]);
			}
			return result;
		}
	}
	
	/**
	 * <p>判断数组是否相等</p>
	 * <p>相等：数组元素全部一致</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 * 
	 * @return 是否相等
	 */
	public static final boolean equals(byte[] sources, byte[] targets) {
		if(sources == targets) {
			return true;
		}
		if(sources == null || targets == null) {
			return false;
		}
		final int length = sources.length;
		if(length != targets.length) {
			return false;
		}
		for (int index = 0; index < length; index++) {
			if(sources[index] != targets[index]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * <p>数组大小比较（无符号）</p>
	 * <p>长度不同时：长度长的数组大</p>
	 * <p>长度相同时：高位大的数组大</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 * 
	 * @return 正数、零、负数代表原始数据大于、等于、小于比较数据
	 */
	public static final int compareUnsigned(byte[] sources, byte[] targets) {
		Objects.requireNonNull(sources, "数组大小比较原始参数错误");
		Objects.requireNonNull(targets, "数组大小比较比较参数错误");
		final int length = sources.length;
		if (length != targets.length) {
			return length > targets.length ? 1 : -1;
		} else {
			for (int index = 0; index < length; index++) {
				if (sources[index] != targets[index]) {
					return ((char) sources[index]) - ((char) targets[index]);
				}
			}
			return 0;
		}
	}
	
	/**
	 * <p>数组是否为空</p>
	 * 
	 * @param objects 数组
	 * 
	 * @return true-空；false-非空；
	 */
	public static final boolean isEmpty(Object[] objects) {
		return objects == null || objects.length == 0;
	}
	
	/**
	 * <p>数组是否非空</p>
	 * 
	 * @param objects 数组
	 * 
	 * @return true-非空；false-空；
	 */
	public static final boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}
	
	/**
	 * <p>字节数组是否为空</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return true-空；false-非空；
	 */
	public static final boolean isEmpty(byte[] bytes) {
		return bytes == null || bytes.length == 0;
	}

	/**
	 * <p>字节数组是否非空</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return true-非空；false-空；
	 */
	public static final boolean isNotEmpty(byte[] bytes) {
		return !isEmpty(bytes);
	}
	
	/**
	 * <p>随机字节数组</p>
	 * 
	 * @param length 数组长度
	 * 
	 * @return 字节数组
	 */
	public static final byte[] random(int length) {
		final byte[] bytes = new byte[length];
		final Random random = NumberUtils.random();
		for (int index = 0; index < length; index++) {
			bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return bytes;
	}
	
	/**
	 * @param values 查找数组
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 * 
	 * @see #NONE_INDEX
	 * @see #indexOf(int[], int, int, int)
	 */
	public static final int indexOf(int[] values, int value) {
		return indexOf(values, 0, values.length, value);
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 * 
	 * @see #NONE_INDEX
	 */
	public static final int indexOf(int[] values, int begin, int end, int value) {
		end = end > values.length ? values.length : end;
		for (int index = begin; index < end; index++) {
			if(values[index] == value) {
				return index;
			}
		}
		return ArrayUtils.NONE_INDEX;
	}
	
	/**
	 * <p>查找数组索引</p>
	 * 
	 * @param values 查找数组
	 * @param value 查找数据
	 * 
	 * @return 数据索引
	 * 
	 * @see #NONE_INDEX
	 */
	public static final int indexOf(char[] values, char value) {
		for (int index = 0; index < values.length; index++) {
			if(values[index] == value) {
				return index;
			}
		}
		return ArrayUtils.NONE_INDEX;
	}
	
}
