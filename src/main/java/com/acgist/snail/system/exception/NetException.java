package com.acgist.snail.system.exception;

/**
 * <p>网络异常</p>
 * <p>用于{@linkplain com.acgist.snail.net 网络协议}中出现的异常。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NetException extends Exception {

	private static final long serialVersionUID = 1L;

	public NetException() {
		super("网络异常");
	}

	public NetException(String message) {
		super(message);
	}

	public NetException(Throwable cause) {
		super(cause);
	}
	
	public NetException(String message, Throwable cause) {
		super(message, cause);
	}

}
