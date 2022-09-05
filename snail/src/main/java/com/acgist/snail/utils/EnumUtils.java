package com.acgist.snail.utils;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 枚举工具
 * 
 * @author acgist
 */
public final class EnumUtils {

	private EnumUtils() {
	}
	
	/**
	 * 创建枚举索引数组
	 * 
	 * @param <T> 枚举泛型
	 * 
	 * @param clazz 枚举类型
	 * @param mapper 索引函数
	 * 
	 * @return 索引数组
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] index(Class<T> clazz, Function<T, Byte> mapper) {
		final Object[] array = clazz.getEnumConstants();
		final int length = Stream.of(array).map(value -> mapper.apply((T) value)).max(Byte::compare).get() + 1;
		final T[] index = (T[]) Array.newInstance(clazz, length);
		for (final Object value : array) {
			index[mapper.apply((T) value)] = (T) value;
		}
		return (T[]) index;
	}
	
}
