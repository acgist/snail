package com.acgist.snail.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.ResultSetWrapper;

/**
 * 实体工具
 */
public class EntityUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityUtils.class);
	
	/**
	 * 获取属性
	 */
	public static final String[] entityProperty(Class<?> clazz) {
		String[] properties = null;
		final Class<?> superClazz = clazz.getSuperclass();
		if(superClazz != null) {
			properties = entityProperty(superClazz);
		} else {
			properties = new String[0];
		}
		final Field[] fields = clazz.getDeclaredFields();
		return Stream.concat(
			Stream
				.of(fields)
				.filter(field -> {
					return !Modifier.isStatic(field.getModifiers()); // 非静态属性
				})
				.map(field -> field.getName()),
			Stream.of(properties)
		).toArray(String[]::new);
	}
	
	/**
	 * 获取属性值
	 */
	public static final Object[] entityPropertyValue(Object entity, String[] properties) {
		return Stream
			.of(properties)
			.map(property -> entityPropertyValue(entity, property))
			.toArray();
	}
	
	/**
	 * 获取属性值
	 */
	public static final Object entityPropertyValue(Object entity, String property) {
		final Class<?> clazz = entity.getClass();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			return descriptor.getReadMethod().invoke(entity);
		} catch (Exception e) {
			LOGGER.error("反射属性获取异常", e);
		}
		return null;
	}
	
	/**
	 * 属性装配
	 */
	public static final void entity(Object entity, ResultSetWrapper wrapper) {
		final Class<?> clazz = entity.getClass();
		final String[] properties = entityProperty(clazz);
		for (String property : properties) {
			try {
				final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
				final Object value = unpack(descriptor.getPropertyType(), wrapper.getObject(property));
				descriptor.getWriteMethod().invoke(entity, value);
			} catch (Exception e) {
				LOGGER.info("反射属性装配异常", e);
			}
		}
	}
	
	/**
	 * 类型转换
	 */
	public static final Object pack(Object object) {
		if(object == null) {
			return null;
		}
		if(object instanceof Enum<?>) { // 枚举类型
			final Enum<?> value = (Enum<?>) object;
			return value.name();
		}
		return object;
	}
	
	/**
	 * 类型转换：TODO：泛型优化
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Object unpack(Class<?> clazz, Object value) {
		if(value == null) {
			return null;
		}
		if(clazz.isEnum()) { // 枚举类型
			return Enum.valueOf((Class<Enum>) clazz, value.toString());
		}
		return value;
	}
	
}
