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
public final class PeerUtils {

	/**
	 * allowedFast固定值
	 */
	private static final int ALLOWED_FAST_MASK = 0xFFFFFF00;
	/**
	 * allowedFast固定值：k（快速允许块长度）
	 */
	private static final int ALLOWED_FAST_K = 10;
	
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
	 * @param length 数据长度
	 */
	public static final Map<String, Integer> read(ByteBuffer buffer, int length) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.position() < length) {
			final String ip = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			data.put(ip, port);
		}
		return data;
	}
	
	/**
	 * 快速允许块索引
	 * 
	 * @param pieceSize 块数量
	 * @param ip Peer的IP地址
	 * @param infoHash 种子InfoHash
	 * 
	 * @return 快速允许块索引
	 */
	public static final int[] allowedFast(int pieceSize, String ipAddress, byte[] infoHash) {
		final int ipValue = NetUtils.encodeIpToInt(ipAddress);
		ByteBuffer buffer = ByteBuffer.allocate(24); // IP(4) + InfoHash(20)
		buffer.putInt(ALLOWED_FAST_MASK & ipValue);
		buffer.put(infoHash);
		int size = 0;
		final int[] seqs = new int[ALLOWED_FAST_K];
		while(size < ALLOWED_FAST_K) {
			buffer = ByteBuffer.wrap(StringUtils.sha1(buffer.array()));
			for (int index = 0; index < 5 && size < ALLOWED_FAST_K; index++) {
				final int seq = (int) (Integer.toUnsignedLong(buffer.getInt()) % pieceSize);
				if(ArrayUtils.indexOf(seqs, 0, size, seq) == ArrayUtils.NO_INDEX) {
					seqs[size++] = seq;
				}
			}
		}
		return seqs;
	}
	
}
