package com.acgist.snail.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.dto.ResultSetWrapper;

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
		Class<?> superClazz = clazz.getSuperclass();
		if(superClazz != null) {
			properties = entityProperty(superClazz);
		} else {
			properties = new String[0];
		}
		Field[] fields = clazz.getDeclaredFields();
		return Stream.concat(
			Stream
				.of(fields)
				.filter(field -> {
					return !Modifier.isStatic(field.getModifiers());
				})
				.map(field -> field.getName()),
			Stream.of(properties)
		).toArray(String[]::new);
	}
	
	/**
	 * 获取属性值
	 */
	public static final Object[] entityPropertyValue(Object entity, String[] properties) {
		return Stream.of(properties)
		.map(property -> entityPropertyValue(entity, property))
		.toArray();
	}
	
	/**
	 * 获取属性值
	 */
	public static final Object entityPropertyValue(Object entity, String property) {
		Class<?> clazz = entity.getClass();
		try {
			PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			return descriptor.getReadMethod().invoke(entity);
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error("反射获取属性异常", e);
		}
		return null;
	}
	
	/**
	 * 属性装配
	 */
	public static final void entity(Object entity, ResultSetWrapper wrapper) {
		Class<?> clazz = entity.getClass();
		String[] properties = entityProperty(clazz);
		for (String property : properties) {
			try {
				PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
				descriptor.getWriteMethod().invoke(entity, wrapper.getObject(property));
			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.info("反射设置属性异常", e);
			}
		}
	}
	
}
