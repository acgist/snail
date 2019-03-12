package com.acgist.snail.system.exception;

/**
 * 网络异常
 */
public class NetException extends Exception {

	private static final long serialVersionUID = 1L;

	public NetException() {
	}

	public NetException(String message) {
		super(message);
	}

	public NetException(Throwable e) {
		super(e);
	}
	
	public NetException(String message, Throwable e) {
		super(message, e);
	}

}
