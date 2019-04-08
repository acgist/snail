package com.acgist.snail.utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Peer读取
 */
public class PeerUtils {

	private static final int MAX_PORT = (int) Math.pow(2, 16);
	
	/**
	 * 读取IP和端口信息
	 */
	public static final Map<String, Integer> read(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return read(buffer, bytes.length);
	}
	
	/**
	 * 读取IP和端口信息
	 */
	public static final Map<String, Integer> read(ByteBuffer buffer, int size) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.position() < size) {
			int ipValue = buffer.getInt();
			int port = Short.valueOf(buffer.getShort()).intValue();
			if(port < 0) {
				port = MAX_PORT + port;
			}
			data.put(NetUtils.intToIp(ipValue), port);
		}
		return data;
	}
	
}
