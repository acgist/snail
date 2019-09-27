package com.acgist.snail.utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Peer工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerUtils {

	/**
	 * 读取IP和端口信息
	 */
	public static final Map<String, Integer> read(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return read(buffer, bytes.length);
	}
	
	/**
	 * 读取IP和端口信息
	 * 
	 * @param buffer 数据
	 * @param size 数据长度
	 */
	public static final Map<String, Integer> read(ByteBuffer buffer, int size) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.position() < size) {
			final String ip = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			data.put(ip, port);
		}
		return data;
	}
	
}
