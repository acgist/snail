package com.acgist.snail.utils;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 枚举工具
 * 主要用来避免使用for循环，通过数组、Map等等数据结构快速映射枚举。
 * 
 * @author acgist
 */
public final class EnumUtils {

	private EnumUtils() {
	}
	
	/**
	 * 创建枚举索引数组
	 * 注意：只有频繁使用枚举转义同时枚举索引和枚举数量差距不大时使用
	 * 
	 * @param <T> 枚举泛型
	 * 
	 * @param clazz 枚举类型
	 * @param mapper 枚举索引函数
	 * 
	 * @return 枚举索引数组
	 * 
	 * TODO：类型安全
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] index(Class<T> clazz, Function<T, Byte> mapper) {
		if(clazz == null || !clazz.isEnum()) {
			throw new IllegalArgumentException("必须输入枚举类型");
		}
		final T[] array = clazz.getEnumConstants();
		final int length = Stream.of(array)
			.map(mapper::apply)
			.map(v -> Byte.toUnsignedInt(v))
			.max(Integer::compare)
			.get() + 1;
		if(array.length * 2 < length || Byte.MAX_VALUE < length) {
			// 如果最大索引大于枚举数量两倍不适合建索引
			throw new IllegalArgumentException("枚举类型不适合建索引");
		}
		final T[] index = (T[]) Array.newInstance(clazz, length);
		for (final T value : array) {
			index[mapper.apply(value)] = value;
		}
		return index;
	}
	
	/**
	 * 创建枚举索引Map
	 * 
	 * @param <T> 枚举泛型
	 * 
	 * @param clazz 枚举类型
	 * @param mapper 枚举索引函数
	 * 
	 * @return 枚举Map索引
	 */
	public static final <T> Map<Integer, T> map(Class<T> clazz, Function<T, Integer> mapper) {
		if(clazz == null || !clazz.isEnum()) {
			throw new IllegalArgumentException("必须输入枚举类型");
		}
		final T[] array = clazz.getEnumConstants();
		return Stream.of(array).collect(Collectors.toMap(v -> mapper.apply(v), UnaryOperator.identity()));
	}
	
}
