package com.acgist.snail.exception;

/**
 * <p>网络异常</p>
 * <p>用途：{@linkplain com.acgist.snail.net 网络协议}</p>
 * 
 * @author acgist
 * @since 1.0.0
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
		super(cause.getMessage(), cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public NetException(String message, Throwable cause) {
		super(message, cause);
	}

}
