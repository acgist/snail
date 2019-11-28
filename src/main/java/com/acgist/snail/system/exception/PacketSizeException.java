package com.acgist.snail.system.exception;

/**
 * <p>网络包数据长度异常</p>
 * <p>数据长度过大或者小于零</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PacketSizeException extends NetException {

	private static final long serialVersionUID = 1L;

	public PacketSizeException() {
		super("网络包数据长度异常");
	}
	
	/**
	 * @param size 数据长度
	 */
	public PacketSizeException(int size) {
		super("网络包数据长度错误：" + size);
	}

	public PacketSizeException(String message) {
		super(message);
	}

	public PacketSizeException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	public PacketSizeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
