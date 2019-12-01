package com.acgist.snail.utils;

import java.beans.PropertyDescriptor;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.h2.jdbc.JdbcClob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.ResultSetWrapper;

/**
 * <p>Bean工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BeanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);
	
	/**
	 * <p>使用反射生成实例：调用默认构造方法（无参）</p>
	 * 
	 * @param <T> 类型泛型
	 * 
	 * @param clazz 类型Class
	 * 
	 * @return 实体
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		if(clazz == null) {
			return null;
		}
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("反射生成实例异常", e);
		}
		return null;
	}
	
	/**
	 * <p>将对象属性转为Map</p>
	 * <dl>
	 * 	<dt>类型转换</dt>
	 * 	<dd>String -&gt; String</dd>
	 * 	<dd>Number -&gt; Number</dd>
	 * 	<dd>Enum   -&gt; String</dd>
	 * 	<dd>Date   -&gt; String(yyyyMMddHHmmss)</dd>
	 * </dl>
	 * 
	 * @param instance 对象
	 * 
	 * @return 对象属性Map
	 */
	public static final Map<String, Object> toMap(Object instance) {
		final Map<String, Object> map = new HashMap<>();
		final String[] properties = properties(instance.getClass());
		final SimpleDateFormat formater = new SimpleDateFormat(DateUtils.DEFAULT_PATTERN);
		for (String property : properties) {
			final Object object = propertyValue(instance, property);
			if(object instanceof Enum<?>) {
				map.put(property, ((Enum<?>) object).name());
			} else if(object instanceof Date) {
				map.put(property, formater.format(object));
			} else {
				map.put(property, object);
			}
		}
		return map;
	}
	
	/**
	 * <p>获取类型属性</p>
	 * <dl>
	 * 	<dt>不获取的属性</dt>
	 * 	<dd>静态：static</dd>
	 * 	<dd>瞬时：transient</dd>
	 * </dl>
	 * 
	 * @param clazz 类型
	 * 
	 * @return 属性数组
	 */
	public static final String[] properties(Class<?> clazz) {
		String[] properties = null;
		final Class<?> superClazz = clazz.getSuperclass(); // 父类
		if(superClazz != null) {
			properties = properties(superClazz);
		} else {
			properties = new String[0];
		}
		final Field[] fields = clazz.getDeclaredFields();
		return Stream.concat(
			Stream
				.of(fields)
				.filter(field -> {
					return 
						!Modifier.isStatic(field.getModifiers()) && // 非静态属性
						!Modifier.isTransient(field.getModifiers()); // 非瞬时属性
				})
				.map(field -> field.getName()),
			Stream.of(properties)
		).toArray(String[]::new);
	}
	
	/**
	 * <p>获取对象属性值</p>
	 * 
	 * @param instance 对象
	 * @param properties 对象属性集合
	 * 
	 * @return 属性值集合
	 */
	public static final Object[] propertiesValue(Object instance, String[] properties) {
		return Stream
			.of(properties)
			.map(property -> propertyValue(instance, property))
			.toArray();
	}
	
	/**
	 * <p>获取属性值</p>
	 * 
	 * @param instance 对象
	 * @param property 对象属性
	 * 
	 * @return 属性值
	 */
	public static final Object propertyValue(Object instance, String property) {
		final Class<?> clazz = instance.getClass();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			return descriptor.getReadMethod().invoke(instance);
		} catch (Exception e) {
			LOGGER.error("反射获取属性值异常", e);
		}
		return null;
	}
	
	/**
	 * <p>属性装配</p>
	 * 
	 * @param instance 对象
	 * @param wrapper 属性包装器
	 */
	public static final void setProperties(Object instance, ResultSetWrapper wrapper) {
		final Class<?> clazz = instance.getClass();
		final String[] properties = properties(clazz);
		for (String property : properties) {
			try {
				final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
				final Object value = unpack(descriptor.getPropertyType(), wrapper.getObject(property));
				descriptor.getWriteMethod().invoke(instance, value);
			} catch (Exception e) {
				LOGGER.info("反射属性装配异常", e);
			}
		}
	}
	
	/**
	 * <p>类型打包</p>
	 * <p>枚举类型转换为字符串类型</p>
	 * 
	 * @param object 属性原始值
	 * 
	 * @return 属性打包值
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
	 * <p>类型拆包</p>
	 * <p>类型：枚举、长字符串</p>
	 * 
	 * @param clazz 属性类型
	 * @param value 属性打包值
	 * 
	 * @return 属性原始值
	 */
	public static final Object unpack(Class<?> clazz, Object value) {
		if(clazz == null || value == null) {
			return null;
		}
		if(clazz.isEnum()) { // 枚举类型
			final var enums = clazz.getEnumConstants();
			// 下面方法存在泛型警告
//			return Enum.valueOf((Class<Enum>) clazz, value.toString());
			for (Object object : enums) {
				// 转换枚举使用name()方法
//				final Enum<?> enumValue = (Enum<?>) object;
//				if(enumValue.name().equals(value.toString())) {
//					return object;
//				}
				// 直接使用toString()方法
				if(object.toString().equals(value.toString())) {
					return object;
				}
			}
			return null;
		}
		if(value instanceof JdbcClob) { // 长字符串
			final JdbcClob clob = (JdbcClob) value;
			try(final Reader reader = clob.getCharacterStream()) {
				int index;
				final char[] chars = new char[1024];
				final StringBuilder builder = new StringBuilder();
				while((index = reader.read(chars)) != -1) {
					builder.append(new String(chars, 0, index));
				}
				return builder.toString();
			} catch (Exception e) {
				LOGGER.error("JdbcClob读取异常", e);
			} finally {
				clob.free();
			}
		}
		return value;
	}
	
}
