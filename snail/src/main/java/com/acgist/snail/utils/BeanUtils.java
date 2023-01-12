package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>Bean工具</p>
 * <p>可以使用Java内省替换</p>
 * 
 * @author acgist
 */
public final class BeanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

	private BeanUtils() {
	}
	
	/**
	 * <p>通过反射生成实例</p>
	 * <p>调用默认无参构造方法</p>
	 * 
	 * @param <T> 类型
	 * 
	 * @param clazz 类型
	 * 
	 * @return 实例
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		Objects.requireNonNull(clazz, "无效类型");
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOGGER.error("通过反射生成实例异常：{}", clazz, e);
		}
		return null;
	}

	/**
	 * 属性类型转换
	 * 
	 * {@code Enum}    {@code String}
	 * {@code Date}    {@code String(yyyy-MM-dd HH:mm:ss)}
	 * {@code byte[]}  {@code String}
	 * 
	 * @param object 原始对象
	 * 
	 * @return 转换对象
	 */
	public static final Object objectToString(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Enum<?> value) {
			return value.name();
		} else if (object instanceof Date date) {
			return DateUtils.dateFormat(date);
		} else if (object instanceof byte[] bytes) {
			return StringUtils.hex(bytes);
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
		if(instance == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(instance.getClass().getSimpleName());
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
	 * <p>获取对象属性数据</p>
	 * 
	 * @param instance 对象
	 * 
	 * @return 属性数据
	 */
	public static final Map<String, Object> toMap(final Object instance) {
		if(instance == null) {
			return Map.of();
		}
		final Map<String, Object> map = new HashMap<>();
		final String[] properties = properties(instance.getClass());
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(instance);
		for (String property : properties) {
			map.put(property, objectToString(descriptor.get(property)));
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
		final Class<?> superClazz = clazz.getSuperclass();
		if(superClazz != null) {
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
	 * <p>获取对象属性值</p>
	 * 
	 * @param instance 对象
	 * @param properties 属性
	 * 
	 * @return 属性值
	 */
	public static final Object[] properties(final Object instance, final String[] properties) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(properties);
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(instance);
		return Stream.of(properties).map(descriptor::get).toArray();
	}
	
	/**
	 * <p>设置对象属性值</p>
	 * 
	 * @param instance 对象
	 * @param properties 属性值
	 */
	public static final void properties(Object instance, Map<String, Object> properties) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(properties);
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(instance);
		properties.forEach(descriptor::set);
	}
	
}
