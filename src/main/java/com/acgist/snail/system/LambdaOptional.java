package com.acgist.snail.system;

/**
 * <p>Lambda表达式用来传值</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class LambdaOptional<T> {

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
	 * <p>设置数据</p>
	 */
	public void set(T t) {
		this.t = t;
	}
	
	/**
	 * <p>获取数据</p>
	 */
	public T get() {
		return this.t;
	}
	
	/**
	 * <p>数据是否为空</p>
	 */
	public boolean isEmpty() {
		return this.t == null;
	}
	
	/**
	 * <p>是否含有数据</p>
	 */
	public boolean isPresent() {
		return !isEmpty();
	}
	
}
