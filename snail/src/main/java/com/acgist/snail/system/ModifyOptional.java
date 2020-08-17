package com.acgist.snail.system;

/**
 * <p>可修改的Optional</p>
 * 
 * @param <T> 数据泛型
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class ModifyOptional<T> {

	/**
	 * <p>数据</p>
	 */
	private T t;
	
	private ModifyOptional() {
	}
	
	private ModifyOptional(T t) {
		this.t = t;
	}

	/**
	 * <p>获取工具对象</p>
	 * 
	 * @param <T> 数据泛型
	 * 
	 * @return 工具对象
	 */
	public static final <T> ModifyOptional<T> newInstance() {
		return new ModifyOptional<>();
	}
	
	/**
	 * <p>获取工具对象</p>
	 * 
	 * @param <T> 数据泛型
	 * 
	 * @param t 数据
	 * 
	 * @return 工具对象
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
	 * <p>判断数据是否为空</p>
	 * 
	 * @return {@code true}-空；{@code false}-非空；
	 */
	public boolean isEmpty() {
		return this.t == null;
	}
	
	/**
	 * <p>判断数据是否非空</p>
	 * 
	 * @return {@code true}-非空；{@code false}-空；
	 */
	public boolean isPresent() {
		return !isEmpty();
	}
	
}
