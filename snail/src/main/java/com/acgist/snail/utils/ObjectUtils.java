package com.acgist.snail.utils;

import java.util.List;
import java.util.Map;

/**
 * <p>Object工具</p>
 * <p>提供重写{@code equals}、{@code toString}、{@code hashCode}等方法</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ObjectUtils {
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private ObjectUtils() {
	}
	
	/**
	 * <p>判断对象是否相等（判断引用）</p>
	 * 
	 * @param source 原始对象：{@code this}
	 * @param target 比较对象
	 * 
	 * @return {@code true}-相等；{@code false}-不等；
	 */
	public static final boolean equals(Object source, Object target) {
		if(source == null) {
			return target == null;
		} else if(source == target) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * <p>计算{@code hashCode}</p>
	 * 
	 * @param values 属性值
	 * 
	 * @return {@code hashCode}
	 */
	public static final int hashCode(Object ... values) {
		if(values == null) {
			return 0;
		}
		final StringBuilder builder = new StringBuilder();
		for (Object object : values) {
			if(object != null) {
				builder.append(object);
			}
		}
		return builder.toString().hashCode();
	}
	
	/**
	 * <p>计算{@code hashCode}</p>
	 * 
	 * @param bytes 字符数组
	 * 
	 * @return {@code hashCode}
	 */
	public static final int hashCode(byte[] bytes) {
		if(bytes == null) {
			return 0;
		}
		int hashCode = 0;
		for (byte value : bytes) {
			hashCode += value;
		}
		return hashCode;
	}
	
	/**
	 * <p>计算{@code toString}</p>
	 * <p>如果{@code values}等于{@code null}，{@code object}必须提供{@code getter}。</p>
	 * 
	 * @param object 对象
	 * @param values 属性值
	 * 
	 * @return {@code toString}
	 */
	public static final String toString(Object object, Object ... values) {
		if(object == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		builder
			.append(object.getClass().getSimpleName())
			.append("[");
		if(ArrayUtils.isEmpty(values)) {
			if(object instanceof List) { // List
				builder.append(object.toString());
			} else if(object instanceof Map) { // Map
				builder.append(object.toString());
			} else {
				// 属性
				final var properties = BeanUtils.properties(object.getClass());
				for (String property : properties) {
					builder
						.append(property)
						.append("=")
						.append(BeanUtils.propertyValue(object, property))
						.append(",");
				}
				builder.setLength(builder.length() - 1);
			}
		} else {
			for (Object value : values) {
				builder.append(value).append(",");
			}
			builder.setLength(builder.length() - 1);
		}
		builder.append("]");
		return builder.toString();
	}
	
}
