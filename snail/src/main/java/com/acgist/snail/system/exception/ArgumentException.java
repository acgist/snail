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

	/**
	 * @param message 错误信息
	 */
	public ArgumentException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public ArgumentException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
