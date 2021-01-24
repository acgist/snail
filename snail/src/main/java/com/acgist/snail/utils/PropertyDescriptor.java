package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>属性工具</p>
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
	 * <p>属性</p>
	 */
	private final String property;
	/**
	 * <p>类型</p>
	 */
	private final Class<?> clazz;
	
	/**
	 * @param property 属性
	 * @param clazz 类型
	 */
	private PropertyDescriptor(String property, Class<?> clazz) {
		this.property = property;
		this.clazz = clazz;
	}
	
	/**
	 * <p>创建属性工具</p>
	 * 
	 * @param property 属性
	 * @param clazz 类型
	 * 
	 * @return {@link PropertyDescriptor}
	 */
	public static final PropertyDescriptor newInstance(String property, Class<?> clazz) {
		return new PropertyDescriptor(property, clazz);
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
	 * @return GETTER
	 */
	public Method getter() {
		final Method[] methods = this.clazz.getMethods();
		final String isMethod = PREFIX_IS + this.property;
		final String getMethod = PREFIX_GET + this.property;
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
	 * @param instance 对象
	 * 
	 * @return 属性值
	 * 
	 * @throws IllegalAccessException 访问异常
	 * @throws IllegalArgumentException 参数异常
	 * @throws InvocationTargetException 反射异常
	 */
	public Object get(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Method getter = this.getter();
		if(getter == null) {
			throw new IllegalArgumentException("属性不存在：" + this.property);
		}
		return getter.invoke(instance);
	}
	
	/**
	 * <p>获取属性SETTER</p>
	 * 
	 * @return SETTER
	 */
	public Method setter() {
		final Method[] methods = this.clazz.getMethods();
		final String setMethod = PREFIX_SET + this.property;
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
	 * @param instance 对象
	 * 
	 * @param value 属性值
	 * 
	 * @throws IllegalAccessException 访问异常
	 * @throws IllegalArgumentException 参数异常
	 * @throws InvocationTargetException 反射异常
	 */
	public void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Method setter = this.setter();
		if(setter == null) {
			throw new IllegalArgumentException("属性不存在：" + this.property);
		}
		setter.invoke(instance, value);
	}
	
	/**
	 * <p>获取属性类型</p>
	 * 
	 * @return 属性类型
	 */
	public Class<?> getPropertyType() {
		Class<?> clazz = this.clazz;
		while(clazz != null) {
			String fieldName;
			final Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				fieldName = field.getName();
				if(!ignoreProperty(field) && fieldName.equals(this.property)) {
					return field.getType();
				}
			}
			// 获取父类属性
			clazz = clazz.getSuperclass();
		}
		return null;
	}
	
}
