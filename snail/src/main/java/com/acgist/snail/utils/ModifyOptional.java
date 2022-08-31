package com.acgist.snail.utils;

/**
 * <p>可修改的Optional</p>
 * 
 * @param <T> 数据类型
 * 
 * @author acgist
 */
public final class ModifyOptional<T> {

	/**
	 * <p>数据</p>
	 */
	private T t;
	
	private ModifyOptional() {
	}
	
	/**
	 * @param t 数据
	 */
	private ModifyOptional(T t) {
		this.t = t;
	}

	/**
	 * <p>获取工具对象</p>
	 * 
	 * @param <T> 数据类型
	 * 
	 * @return {@link ModifyOptional}
	 */
	public static final <T> ModifyOptional<T> newInstance() {
		return new ModifyOptional<>();
	}
	
	/**
	 * <p>获取工具对象</p>
	 * 
	 * @param <T> 数据类型
	 * 
	 * @param t 数据
	 * 
	 * @return {@link ModifyOptional}
	 */
	public static final <T> ModifyOptional<T> newInstance(T t) {
		return new ModifyOptional<>(t);
	}
	
	/**
	 * <p>设置数据</p>
	 * 
	 * @param t 数据
	 */
	public void set(T t) {
		this.t = t;
	}
	
	/**
	 * <p>获取数据</p>
	 * 
	 * @return 数据
	 */
	public T get() {
		return this.t;
	}
	
	/**
	 * 删除旧的数据
	 * 
	 * @return 旧的数据
	 */
	public T delete() {
		final T old = this.t;
		this.t = null;
		return old;
	}

	/**
	 * <p>获取数据</p>
	 * 
	 * @param defaultValue 默认数据
	 * 
	 * @return 数据
	 */
	public T get(T defaultValue) {
		return this.isEmpty() ? defaultValue : this.get();
	}
	
	/**
	 * <p>判断数据是否为空</p>
	 * 
	 * @return 数据是否为空
	 */
	public boolean isEmpty() {
		return this.t == null;
	}
	
	/**
	 * <p>判断数据是否非空</p>
	 * 
	 * @return 数据是否非空
	 */
	public boolean isPresent() {
		return !this.isEmpty();
	}
	
}
