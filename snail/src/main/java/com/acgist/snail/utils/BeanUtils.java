package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Bean工具</p>
 * 
 * @author acgist
 */
public final class BeanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

	/**
	 * <p>工具类禁止实例化</p>
	 */
	private BeanUtils() {
	}
	
	/**
	 * <p>通过反射生成实例</p>
	 * <p>调用默认无参构造方法</p>
	 * 
	 * @param <T> 类型泛型
	 * 
	 * @param clazz 类型
	 * 
	 * @return 实例
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		Objects.requireNonNull(clazz);
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("通过反射生成实例异常：{}", clazz, e);
		}
		return null;
	}

	/**
	 * <p>属性类型转换</p>
	 * 
	 * <table border="1">
	 * 	<caption>属性类型转换</caption>
	 * 	<tr>
	 * 		<td>{@code Enum}</td>
	 * 		<td>{@code String}</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Date}</td>
	 * 		<td>{@code String(yyyyMMddHHmmss)}</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code byte[]}</td>
	 * 		<td>{@code String}</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param object 原始对象
	 * 
	 * @return 转换对象
	 */
	public static final Object objectToString(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Enum<?>) {
			return ((Enum<?>) object).name();
		} else if (object instanceof Date) {
			return DateUtils.dateFormat((Date) object);
		} else if (object instanceof byte[]) {
			return new String((byte[]) object);
		} else {
			return object;
		}
	}
	
	/**
	 * <p>重写对象toString方法</p>
	 * 
	 * @param instance 对象
	 * @param values 属性
	 * 
	 * @return toString
	 */
	public static final String toString(Object instance, Object ... values) {
		Objects.requireNonNull(instance);
		final StringBuilder builder = new StringBuilder(instance.getClass().toString());
		builder.append("@");
		if (ArrayUtils.isEmpty(values)) {
			builder.append(toMap(instance).toString());
		} else {
			builder.append("{");
			for (Object object : values) {
				builder.append(objectToString(object)).append(", ");
			}
			builder.setLength(builder.length() - 2);
			builder.append("}");
		}
		return builder.toString();
	}
	
	/**
	 * <p>获取对象属性Map</p>
	 * 
	 * @param instance 对象
	 * 
	 * @return 属性Map
	 */
	public static final Map<String, Object> toMap(final Object instance) {
		Objects.requireNonNull(instance);
		final Map<String, Object> map = new HashMap<>();
		final String[] properties = properties(instance.getClass());
		for (String property : properties) {
			final Object object = propertyValue(instance, property);
			map.put(property, objectToString(object));
		}
		return map;
	}
	
	/**
	 * <p>获取类型所有属性名称</p>
	 * 
	 * @param clazz 类型
	 * 
	 * @return 所有属性名称
	 */
	public static final String[] properties(final Class<?> clazz) {
		Objects.requireNonNull(clazz);
		String[] properties = null;
		final Class<?> superClazz = clazz.getSuperclass(); // 父类
		if(superClazz != null) {
			// 递归获取属性
			properties = properties(superClazz);
		} else {
			properties = new String[0];
		}
		final Field[] fields = clazz.getDeclaredFields();
		return Stream.concat(
			Stream.of(fields)
				.filter(field -> !PropertyDescriptor.ignoreProperty(field))
				.map(Field::getName),
			Stream.of(properties)
		).toArray(String[]::new);
	}
	
	/**
	 * <p>获取对象指定属性的属性值</p>
	 * 
	 * @param instance 对象
	 * @param properties 属性
	 * 
	 * @return 属性值
	 */
	public static final Object[] propertiesValue(final Object instance, final String[] properties) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(properties);
		return Stream.of(properties)
			.map(property -> propertyValue(instance, property))
			.toArray();
	}
	
	/**
	 * <p>获取对象指定属性的属性值</p>
	 * 
	 * @param instance 对象
	 * @param property 属性
	 * 
	 * @return 属性值
	 */
	public static final Object propertyValue(final Object instance, final String property) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(property);
		final Class<?> clazz = instance.getClass();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			final Method method = descriptor.getReadMethod();
			if(method != null) {
				return method.invoke(instance);
			}
		} catch (Exception e) {
			LOGGER.error("获取对象指定属性的属性值异常：{}-{}", clazz, property, e);
		}
		return null;
	}
	
	/**
	 * <p>设置对象属性</p>
	 * 
	 * @param instance 对象
	 * @param data 属性
	 */
	public static final void properties(Object instance, Map<String, Object> data) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(data);
		final Class<?> clazz = instance.getClass();
		final String[] properties = properties(clazz);
		for (String property : properties) {
			try {
				final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
				final Method method = descriptor.getWriteMethod();
				if(method != null) {
					method.invoke(instance, data.get(property));
				}
			} catch (Exception e) {
				LOGGER.error("设置对象属性异常：{}-{}", clazz, property, e);
			}
		}
	}
	
}
