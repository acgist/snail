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

	public RepositoryException() {
		super("数据库异常");
	}
	
	/**
	 * <p>判断参数是否为空，如果不为空抛出异常。</p>
	 * 
	 * @param object 参数
	 */
	public static final void isNull(Object object) {
		if(object != null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}
	
	/**
	 * <p>判断参数是否不为空，如果为空抛出异常。</p>
	 * 
	 * @param object 参数
	 */
	public static final void isNotNull(Object object) {
		if(object == null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}

	public RepositoryException(String message) {
		super(message);
	}

	public RepositoryException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
