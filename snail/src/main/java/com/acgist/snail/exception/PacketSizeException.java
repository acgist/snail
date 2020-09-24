package com.acgist.snail.exception;

import com.acgist.snail.config.SystemConfig;

/**
 * <p>网络包数据长度异常</p>
 * <p>数据长度过大或者小于零</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PacketSizeException extends NetException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>验证网络包大小</p>
	 * <p>网络包大小格式错误抛出异常：{@link PacketSizeException}</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static final void verify(short length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}
	
	/**
	 * <p>验证网络包大小</p>
	 * <p>网络包大小格式错误抛出异常：{@link PacketSizeException}</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static final void verify(int length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}

	public PacketSizeException() {
		super("网络包数据长度异常");
	}
	
	/**
	 * @param size 数据长度
	 */
	public PacketSizeException(int size) {
		super("网络包数据长度错误：" + size);
	}

	/**
	 * @param message 错误信息
	 */
	public PacketSizeException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public PacketSizeException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public PacketSizeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
