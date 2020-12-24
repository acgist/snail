package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
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
	 * <p>获取实例属性Map</p>
	 * 
	 * <table border="1">
	 * 	<caption>属性类型转换</caption>
	 * 	<tr>
	 * 		<td>{@code String}</td>
	 * 		<td>{@code String}</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Number}</td>
	 * 		<td>{@code Number}</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Enum}</td>
	 * 		<td>{@code String}</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Date}</td>
	 * 		<td>{@code String(yyyyMMddHHmmss)}</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param instance 实例
	 * 
	 * @return 属性Map
	 */
	public static final Map<String, Object> toMap(final Object instance) {
		Objects.requireNonNull(instance);
		final Map<String, Object> map = new HashMap<>();
		final String[] properties = properties(instance.getClass());
		for (String property : properties) {
			final Object object = propertyValue(instance, property);
			if(object instanceof Enum<?>) {
				map.put(property, ((Enum<?>) object).name());
			} else if(object instanceof Date) {
				// TODO：强转使用JDK最新写法
				// TODO：使用DateUtil工具替换
				final SimpleDateFormat formater = new SimpleDateFormat(DateUtils.DEFAULT_PATTERN);
				map.put(property, formater.format(object));
			} else {
				map.put(property, object);
			}
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
	 * <p>获取实例对象指定属性名称的属性值</p>
	 * 
	 * @param instance 实例
	 * @param properties 属性名称
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
	 * <p>获取实例对象指定属性名称的属性值</p>
	 * 
	 * @param instance 实例
	 * @param property 属性名称
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
			LOGGER.error("获取实例对象指定属性名称的属性值异常：{}-{}", clazz, property, e);
		}
		return null;
	}
	
	/**
	 * <p>设置实例属性</p>
	 * 
	 * @param instance 实例
	 * @param wrapper 属性
	 */
	public static final void setProperties(Object instance, Map<String, Object> data) {
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
				LOGGER.info("设置实例属性异常：{}-{}", clazz, property, e);
			}
		}
	}
	
}
