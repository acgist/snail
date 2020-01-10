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
	 * <p>工具类禁止实例化</p>
	 */
	private PeerUtils() {
	}
	
	/**
	 * <p>allowedFast固定值：{@value}</p>
	 */
	private static final int ALLOWED_FAST_MASK = 0xFFFFFF00;
	/**
	 * <p>allowedFast快速允许Piece长度（k）：{@value}</p>
	 */
	private static final int ALLOWED_FAST_K = 10;
	
	/**
	 * @param bytes 数据
	 * 
	 * @return IP=端口
	 * 
	 * @see #read(ByteBuffer, int)
	 */
	public static final Map<String, Integer> read(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return read(buffer, bytes.length);
	}
	
	/**
	 * <p>读取IP和端口</p>
	 * 
	 * @param buffer 数据
	 * @param length 数据长度
	 * 
	 * @return IP=端口
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
	 * <p>计算快速允许Piece索引</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0006.html</p>
	 * 
	 * @param pieceSize Piece数量：种子Piece总量
	 * @param ipAddress IP地址：Peer地址
	 * @param infoHash InfoHash
	 * 
	 * @return 快速允许Piece索引
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
