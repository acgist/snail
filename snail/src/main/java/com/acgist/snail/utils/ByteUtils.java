package com.acgist.snail.utils;

import java.nio.ByteBuffer;

/**
 * <p>字符工具</p>
 * 
 * @author acgist
 */
public final class ByteUtils {

	private ByteUtils() {
	}
	
	/**
	 * <p>读取剩余字节数据</p>
	 * 
	 * @param buffer 缓冲数据
	 * 
	 * @return 剩余字节数据
	 */
	public static final byte[] remainingToBytes(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}
	
	/**
	 * <p>读取剩余字节数据转为字符串</p>
	 * 
	 * @param buffer 缓冲数据
	 * 
	 * @return 剩余字节数据字符串
	 */
	public static final String remainingToString(ByteBuffer buffer) {
		return new String(remainingToBytes(buffer));
	}
	
	/**
	 * 读取int字节数据
	 * 
	 * @param buffer 缓冲数据
	 * 
	 * @return int字节数据
	 */
	public static final byte[] intToBytes(ByteBuffer buffer) {
		final int length = buffer.getInt();
		final byte[] bytes = new byte[length];
		buffer.get(bytes);
		return bytes;
	}
	
	/**
	 * 读取int字节数据转为字符串
	 * 
	 * @param buffer 缓冲数据
	 * 
	 * @return int字节数据字符串
	 */
	public static final String intToString(ByteBuffer buffer) {
		return new String(intToBytes(buffer));
	}
	
}
