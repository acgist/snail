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
	 * <p>工具类禁止实例化</p>
	 */
	private BeanUtils() {
	}
	
	/**
	 * <p>通过反射生成{@code clazz}实例对象</p>
	 * <p>调用默认无参构造方法</p>
	 * 
	 * @param <T> 类型泛型
	 * @param clazz 对象类型
	 * 
	 * @return 对象
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		if(clazz == null) {
			return null;
		}
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("反射生成实例对象异常", e);
		}
		return null;
	}
	
	/**
	 * <p>将实例对象所有属性转为{@code Map}</p>
	 * 
	 * <table border="1" summary="属性类型转换">
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
	 * @param instance 实例对象
	 * 
	 * @return 对象属性{@code Map}
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
	 * <p>获取类型所有属性</p>
	 * <dl>
	 * 	<dt>不获取的属性</dt>
	 * 	<dd>静态：{@code static}</dd>
	 * 	<dd>瞬时：{@code transient}</dd>
	 * </dl>
	 * 
	 * @param clazz 类型
	 * 
	 * @return 属性数组
	 */
	public static final String[] properties(Class<?> clazz) {
		String[] properties = null;
		final Class<?> superClazz = clazz.getSuperclass(); // 父类
		if(superClazz != null) { // 递归获取属性
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
	 * <p>获取实例对象指定属性的属性值</p>
	 * 
	 * @param instance 实例对象
	 * @param properties 属性数组
	 * 
	 * @return 属性值数组
	 */
	public static final Object[] propertiesValue(Object instance, String[] properties) {
		return Stream
			.of(properties)
			.map(property -> propertyValue(instance, property))
			.toArray();
	}
	
	/**
	 * <p>获取实例对象指定的属性值</p>
	 * 
	 * @param instance 实例对象
	 * @param property 属性名称
	 * 
	 * @return 属性值
	 */
	public static final Object propertyValue(Object instance, String property) {
		final Class<?> clazz = instance.getClass();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			return descriptor.getReadMethod().invoke(instance);
		} catch (Exception e) {
			LOGGER.error("获取属性值异常", e);
		}
		return null;
	}
	
	/**
	 * <p>属性装配</p>
	 * 
	 * @param instance 实例对象
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
				LOGGER.info("属性装配异常", e);
			}
		}
	}
	
	/**
	 * <p>类型打包</p>
	 * <p>处理类型：枚举</p>
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
	 * <p>处理类型：枚举、长字符串</p>
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
		// 枚举类型
		if(clazz.isEnum()) {
			return unpackEnum(clazz, value);
		}
		// 长字符串
		if(value instanceof JdbcClob) {
			return unpackJdbcClob(clazz, value);
		}
		return value;
	}
	
	private static final Object unpackEnum(Class<?> clazz, Object value) {
		final var enums = clazz.getEnumConstants();
		for (Object object : enums) {
			if(object.toString().equals(value.toString())) {
				return object;
			}
		}
		return null;
	}
	
	private static final Object unpackJdbcClob(Class<?> clazz, Object value) {
		int index;
		final JdbcClob clob = (JdbcClob) value;
		final StringBuilder builder = new StringBuilder();
		try(final Reader reader = clob.getCharacterStream()) {
			final char[] chars = new char[1024];
			while((index = reader.read(chars)) != -1) {
				builder.append(new String(chars, 0, index));
			}
		} catch (Exception e) {
			LOGGER.error("读取JdbcClob数据异常", e);
		} finally {
			clob.free();
		}
		return builder.toString();
	}
	
}
