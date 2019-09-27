package com.acgist.snail.system.exception;

/**
 * <p>超过最大网络包大小异常</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class OversizePacketException extends NetException {

	private static final long serialVersionUID = 1L;

	public OversizePacketException() {
		super("超过最大网络包大小异常");
	}
	
	/**
	 * @param size 包大小
	 */
	public OversizePacketException(int size) {
		super("超过最大网络包大小：" + size);
	}

	public OversizePacketException(String message) {
		super(message);
	}

	public OversizePacketException(Throwable cause) {
		super(cause);
	}
	
	public OversizePacketException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
