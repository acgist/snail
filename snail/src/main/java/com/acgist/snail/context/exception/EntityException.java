package com.acgist.snail.context.exception;

/**
 * <p>实体异常</p>
 * 
 * @author acgist
 */
public class EntityException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>验证参数必须为空</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNull(Object object) {
		if(object != null) {
			throw new EntityException("参数必须为空：" + object);
		}
	}
	
	/**
	 * <p>验证参数不能为空</p>
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
	 * <p>实体异常</p>
	 * 
	 * @param message 错误信息
	 */
	public EntityException(String message) {
		super(message);
	}

	/**
	 * <p>实体异常</p>
	 * 
	 * @param cause 原始异常
	 */
	public EntityException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * <p>实体异常</p>
	 * 
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public EntityException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
