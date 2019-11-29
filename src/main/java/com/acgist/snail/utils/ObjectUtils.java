package com.acgist.snail.utils;

import java.util.List;
import java.util.Map;

/**
 * <p>Object工具</p>
 * <p>提供重写toString、equals、hashCode等方法</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ObjectUtils {

	/**
	 * <p>重写hashCode方法</p>
	 * 
	 * @param values 属性
	 * 
	 * @return hashCode
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
	 * <p>对象是否相等（判断引用）</p>
	 * 
	 * @param source 原始对象：this
	 * @param target 比较对象
	 * 
	 * @return true-相等；false-不相等；
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
	 * <p>对象是否可以相互访问</p>
	 * <p>不推荐使用：使用instanceof替代</p>
	 * 
	 * @param source 原始对象：this（父类）
	 * @param target 比较对象（子类）
	 * 
	 * @return true-可以访问；false-不可以访问；
	 */
	@Deprecated
	public static final boolean assignableClazz(Object source, Object target) {
		if(source.getClass().isAssignableFrom(target.getClass())) {
			return true;
		}
		return false;
	}
	
	/**
	 * <p>重写toString方法</p>
	 * 
	 * @param object 对象
	 * @param values 属性
	 * 
	 * @return toString
	 */
	public static final String toString(Object object, Object ... values) {
		final StringBuilder builder = new StringBuilder();
		builder.append(object.getClass().getSimpleName());
		builder.append("=[");
		if(values.length > 0) {
			for (Object value : values) {
				builder.append(value).append(",");
			}
			builder.setLength(builder.length() - 1);
		}
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * <p>重写toString方法</p>
	 * <p>注：对象属性需要提供getter</p>
	 * 
	 * @param object 对象
	 * 
	 * @return toString
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
			final StringBuilder builder = new StringBuilder();
			builder
				.append(object.getClass().getSimpleName())
				.append("[");
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
