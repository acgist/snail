package com.acgist.snail.context.exception;

/**
 * 实体异常
 * 
 * @author acgist
 */
public class EntityException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 验证参数必须为空
	 * 
	 * @param object 参数
	 */
	public static final void requireNull(Object object) {
		if(object != null) {
			throw new EntityException("参数必须为空：" + object);
		}
	}
	
	/**
	 * 验证参数不能为空
	 * 
	 * @param object 参数
	 */
	public static final void requireNotNull(Object object) {
		if(object == null) {
			throw new EntityException("参数不能为空：" + object);
		}
	}
	
	public EntityException() {
		super("实体异常");
	}

	/**
	 * @param message 错误信息
	 */
	public EntityException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public EntityException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public EntityException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
