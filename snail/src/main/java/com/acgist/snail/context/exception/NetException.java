package com.acgist.snail.context.exception;

/**
 * 网络异常
 * 
 * @author acgist
 */
public class NetException extends Exception {

	private static final long serialVersionUID = 1L;

	public NetException() {
		super("网络异常");
	}

	/**
	 * @param message 错误信息
	 */
	public NetException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public NetException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public NetException(String message, Throwable cause) {
		super(message, cause);
	}

}
