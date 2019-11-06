package com.acgist.snail.pojo;

/**
 * <p>Lambda表达式用来传值</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class LambdaOptional<T> {

	/**
	 * 数据
	 */
	private T t;
	
	private LambdaOptional() {
	}
	
	private LambdaOptional(T t) {
		this.t = t;
	}

	public static final <T> LambdaOptional<T> newInstance() {
		return new LambdaOptional<>();
	}
	
	public static final <T> LambdaOptional<T> newInstance(T t) {
		return new LambdaOptional<>(t);
	}
	
	/**
	 * 设置数据
	 */
	public void set(T t) {
		this.t = t;
	}
	
	/**
	 * 获取数据
	 */
	public T get() {
		return this.t;
	}
	
	/**
	 * 没有数据
	 */
	public boolean isEmpty() {
		return this.t == null;
	}
	
	/**
	 * 有数据
	 */
	public boolean isPresent() {
		return !isEmpty();
	}
	
}
