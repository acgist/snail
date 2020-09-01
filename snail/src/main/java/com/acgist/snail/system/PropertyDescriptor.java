package com.acgist.snail.system;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>属性操作</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class PropertyDescriptor {

	/**
	 * <p>方法前缀：{@value}</p>
	 */
	private static final String PREFIX_IS = "is";
	/**
	 * <p>方法前缀：{@value}</p>
	 */
	private static final String PREFIX_GET = "get";
	/**
	 * <p>方法前缀：{@value}</p>
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
	
	public PropertyDescriptor(String property, Class<?> clazz) {
		this.property = property;
		this.clazz = clazz;
	}
	
	/**
	 * <p>忽略属性</p>
	 * <dl>
	 * 	<dd>静态：{@code static}</dd>
	 * 	<dd>瞬时：{@code transient}</dd>
	 * </dl>
	 * 
	 * @param field 属性
	 * 
	 * @return 是否忽略
	 */
	public static final boolean ignoreProperty(Field field) {
		return 
			Modifier.isStatic(field.getModifiers()) || // 静态属性
			Modifier.isTransient(field.getModifiers()); // 瞬时属性
	}
	
	/**
	 * <p>获取属性GET方法</p>
	 * 
	 * @return GET方法
	 */
	public Method getReadMethod() {
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
		throw new IllegalArgumentException(this.notFoundProperty());
	}
	
	/**
	 * <p>获取属性SET方法</p>
	 * 
	 * @return SET方法
	 */
	public Method getWriteMethod() {
		final Method[] methods = this.clazz.getMethods();
		final String setMethod = PREFIX_SET + this.property;
		String methodName;
		for (Method method : methods) {
			methodName = method.getName();
			if(setMethod.equalsIgnoreCase(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException(this.notFoundProperty());
	}

	/**
	 * <p>获取属性类型</p>
	 * 
	 * @return 属性类型
	 */
	public Class<?> getPropertyType() {
		Class<?> clazz = this.clazz;
		while(true) {
			if(clazz == null) {
				break;
			}
			final Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if(!ignoreProperty(field) && field.getName().equals(this.property)) {
					return field.getType();
				}
			}
			clazz = clazz.getSuperclass();
		}
		throw new IllegalArgumentException(this.notFoundProperty());
	}
	
	/**
	 * <p>获取异常信息</p>
	 */
	private String notFoundProperty() {
		return "不存在的属性：" + this.property;
	}
	
}
