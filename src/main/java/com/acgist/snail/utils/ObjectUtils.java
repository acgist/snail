package com.acgist.snail.utils;

import java.util.List;
import java.util.Map;

/**
 * Object工具：toString、equals、hashCode等方法
 */
public class ObjectUtils {

	/**
	 * 重写hashCode方法。
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
	 * 判断是否相等，判断引用。
	 * 
	 * @param source 源：this
	 * @param target 比较对象
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
	 * 判断对象是否可以相互访问，使用instanceof替代。
	 * 
	 * @param source 源：this，父类
	 * @param target 比较对象，子类
	 */
	@Deprecated
	public static final boolean assignableClazz(Object source, Object target) {
		if(source.getClass().isAssignableFrom(target.getClass())) {
			return true;
		}
		return false;
	}
	
	/**
	 * equals对象id生成
	 */
	public static final String equalsBuilder(Object ... objects) {
		if(objects == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			if(object != null) {
				builder.append(object);
			}
		}
		return builder.toString();
	}
	
	/**
	 * toString，必须属性提供对应的get方法。
	 */
	public static final String toString(Object object) {
		if(object == null) {
			return null;
		}
		if(object instanceof List) {
			return object.toString();
		} else if(object instanceof Map) {
			return object.toString();
		} else {
			final StringBuilder builder = new StringBuilder("[");
			final var properties = BeanUtils.properties(object.getClass());
			for (String property : properties) {
				builder.append(property).append("=").append(BeanUtils.propertyValue(object, property)).append(",");
			}
			final int length = builder.length();
			if(length > 1) {
				builder.setLength(length - 1);
				builder.append("]");
			}
			return builder.toString();
		}
	}
	
}
