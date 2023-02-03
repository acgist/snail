package com.acgist.snail.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * List工具
 * 
 * @author acgist
 */
public final class ListUtils {

	private ListUtils() {
	}
	
	/**
	 * @param <T> 对象泛型
	 * 
	 * @param list List
	 * 
	 * @return 头部对象
	 */
	public static final <T> T first(List<T> list) {
		return list.get(0);
	}
	
	/**
	 * @param <T> 对象泛型
	 * 
	 * @param list List
	 * 
	 * @return 尾部对象
	 */
	public static final <T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 对象
	 */
	public static final Object get(List<?> list, int index) {
		if(badIndex(list, index)) {
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 字节
	 */
	public static final Byte getByte(List<?> list, int index) {
		final Long value = getLong(list, index);
		if(value == null) {
			return null;
		}
		return value.byteValue();
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 数值
	 */
	public static final Integer getInteger(List<?> list, int index) {
		final Long value = getLong(list, index);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 数值
	 */
	public static final Long getLong(List<?> list, int index) {
		if(badIndex(list, index)) {
			return null;
		}
		return (Long) list.get(index);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 字符串
	 */
	public static final String getString(List<?> list, int index) {
		return getString(list, index, null);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static final String getString(List<?> list, int index, String encoding) {
		final var bytes = getBytes(list, index);
		if(bytes == null) {
			return null;
		}
		return StringUtils.getCharsetString(bytes, encoding);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 字节数组
	 */
	public static final byte[] getBytes(List<?> list, int index) {
		if(badIndex(list, index)) {
			return null;
		}
		return (byte[]) list.get(index);
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return 集合
	 */
	public static final List<Object> getList(List<?> list, int index) {
		if(badIndex(list, index)) {
			return List.of();
		}
		final var result = (List<?>) list.get(index);
		if(result == null) {
			return List.of();
		}
		return result.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * @param list List
	 * @param index 索引
	 * 
	 * @return Map
	 */
	public static final Map<String, Object> getMap(List<?> list, int index) {
		if(badIndex(list, index)) {
			return Map.of();
		}
		final var result = (Map<?, ?>) list.get(index);
		if(result == null) {
			return Map.of();
		}
		// 使用LinkedHashMap防止乱序
		return result.entrySet().stream()
			.collect(Collectors.toMap(
				entry -> (String) entry.getKey(),
//				entry -> entry.getKey() == null ? null : entry.getKey().toString(),
				Map.Entry::getValue,
				(a, b) -> b,
				LinkedHashMap::new
			));
	}
	
	/**
	 * @param list 集合
	 * @param index 索引
	 * 
	 * @return 是否无效索引
	 */
	private static final boolean badIndex(List<?> list, int index) {
		if(list == null || list.size() <= index) {
			return true;
		}
		return false;
	}
	
}
