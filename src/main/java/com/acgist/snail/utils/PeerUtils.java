package com.acgist.snail.utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Peer读取
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
	 */
	public static final Map<String, Integer> read(ByteBuffer buffer, int size) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.position() < size) {
			final int ipValue = buffer.getInt();
			final int port = NetUtils.decodePort(Short.valueOf(buffer.getShort()));
			data.put(NetUtils.decodeIntToIp(ipValue), port);
		}
		return data;
	}
	
}
