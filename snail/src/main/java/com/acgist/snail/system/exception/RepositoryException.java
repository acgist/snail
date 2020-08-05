package com.acgist.snail.system.exception;

/**
 * <p>数据库异常</p>
 * <p>用途：{@linkplain com.acgist.snail.repository 数据库}</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class RepositoryException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>判断参数是否为空</p>
	 * <p>如果不为空抛出{@linkplain RepositoryException 异常}</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNull(Object object) {
		if(object != null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}
	
	/**
	 * <p>判断参数是否不为空</p>
	 * <p>如果为空抛出{@linkplain RepositoryException 异常}</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNotNull(Object object) {
		if(object == null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}
	
	public RepositoryException() {
		super("数据库异常");
	}

	/**
	 * @param message 错误信息
	 */
	public RepositoryException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public RepositoryException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
