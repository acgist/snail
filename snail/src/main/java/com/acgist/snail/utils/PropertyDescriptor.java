package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>对象属性工具</p>
 * 
 * @author acgist
 */
public final class PropertyDescriptor {

	/**
	 * <p>GETTER前缀（Boolean）：{@value}</p>
	 */
	private static final String PREFIX_IS = "is";
	/**
	 * <p>GETTER前缀：{@value}</p>
	 */
	private static final String PREFIX_GET = "get";
	/**
	 * <p>SETTER前缀：{@value}</p>
	 */
	private static final String PREFIX_SET = "set";
	
	/**
	 * <p>类型</p>
	 */
	private final Class<?> clazz;
	/**
	 * <p>对象</p>
	 */
	private final Object instance;
	
	/**
	 * @param instance 对象
	 */
	private PropertyDescriptor(Object instance) {
		this.instance = instance;
		this.clazz = instance.getClass();
	}
	
	/**
	 * <p>创建对象属性工具</p>
	 * 
	 * @param instance 对象
	 * 
	 * @return {@link PropertyDescriptor}
	 */
	public static final PropertyDescriptor newInstance(Object instance) {
		return new PropertyDescriptor(instance);
	}
	
	/**
	 * <p>忽略属性</p>
	 * <dl>
	 * 	<dd>静态：static</dd>
	 * 	<dd>瞬时：transient</dd>
	 * </dl>
	 * 
	 * @param field 属性
	 * 
	 * @return 是否忽略
	 */
	public static final boolean ignoreProperty(Field field) {
		return
			// 静态属性
			Modifier.isStatic(field.getModifiers()) ||
			// 瞬时属性
			Modifier.isTransient(field.getModifiers());
	}
	
	/**
	 * <p>获取属性GETTER</p>
	 * 
	 * @param property 属性名称
	 * 
	 * @return GETTER
	 */
	public Method getter(String property) {
		final Method[] methods = this.clazz.getMethods();
		final String isMethod = PREFIX_IS + property;
		final String getMethod = PREFIX_GET + property;
		String methodName;
		for (Method method : methods) {
			methodName = method.getName();
			// GET方法最多优先判断
			if(
				getMethod.equalsIgnoreCase(methodName) ||
				isMethod.equalsIgnoreCase(methodName)
			) {
				return method;
			}
		}
		return null;
	}

	/**
	 * <p>获取属性值</p>
	 * 
	 * @param property 属性名称
	 * 
	 * @return 属性值
	 * 
	 * @throws IllegalAccessException 访问异常
	 * @throws InvocationTargetException 反射异常
	 */
	public Object get(String property) throws IllegalAccessException, InvocationTargetException {
		final Method getter = this.getter(property);
		if(getter == null) {
			throw new IllegalArgumentException("属性不存在：" + property);
		}
		return getter.invoke(this.instance);
	}
	
	/**
	 * <p>获取属性SETTER</p>
	 * 
	 * @param property 属性名称
	 * 
	 * @return SETTER
	 */
	public Method setter(String property) {
		final Method[] methods = this.clazz.getMethods();
		final String setMethod = PREFIX_SET + property;
		String methodName;
		for (Method method : methods) {
			methodName = method.getName();
			if(setMethod.equalsIgnoreCase(methodName)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * <p>设置属性值</p>
	 * 
	 * @param property 属性名称
	 * @param value 属性值
	 * 
	 * @throws IllegalAccessException 访问异常
	 * @throws InvocationTargetException 反射异常
	 */
	public void set(String property, Object value) throws IllegalAccessException, InvocationTargetException {
		final Method setter = this.setter(property);
		if(setter == null) {
			throw new IllegalArgumentException("属性不存在：" + property);
		}
		setter.invoke(this.instance, value);
	}
	
	/**
	 * <p>获取属性类型</p>
	 * 
	 * @param property 属性名称
	 * 
	 * @return 属性类型
	 */
	public Class<?> getPropertyType(String property) {
		String fieldName;
		Class<?> parentClazz = this.clazz;
		while(parentClazz != null) {
			final Field[] fields = parentClazz.getDeclaredFields();
			for (Field field : fields) {
				fieldName = field.getName();
				if(!ignoreProperty(field) && fieldName.equals(property)) {
					return field.getType();
				}
			}
			parentClazz = parentClazz.getSuperclass();
		}
		return null;
	}
	
}
