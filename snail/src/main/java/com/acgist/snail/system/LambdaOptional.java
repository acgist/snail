package com.acgist.snail.system;

/**
 * <p>Lambda表达式工具</p>
 * <p>用来传递数据</p>
 * 
 * @param <T> 数据泛型
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class LambdaOptional<T> {

	/**
	 * <p>数据</p>
	 */
	private T t;
	
	private LambdaOptional() {
	}
	
	private LambdaOptional(T t) {
		this.t = t;
	}

	/**
	 * <p>获取工具对象</p>
	 * 
	 * @param <T> 数据泛型
	 * 
	 * @return 工具对象
	 */
	public static final <T> LambdaOptional<T> newInstance() {
		return new LambdaOptional<>();
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
	public static final <T> LambdaOptional<T> newInstance(T t) {
		return new LambdaOptional<>(t);
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
