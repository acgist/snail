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
