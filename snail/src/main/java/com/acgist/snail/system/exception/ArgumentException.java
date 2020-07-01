package com.acgist.snail.system.exception;

/**
 * <p>参数异常</p>
 * <p>常见异常：不支持的算法、不支持的参数类型、参数数量错误等等</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ArgumentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArgumentException() {
		super("参数异常");
	}

	public ArgumentException(String message) {
		super(message);
	}

	public ArgumentException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
