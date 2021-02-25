package com.acgist.snail.utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.BEncodeDecoder;

/**
 * <p>Peer工具</p>
 * 
 * @author acgist
 */
public final class PeerUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerUtils.class);
	
	private PeerUtils() {
	}
	
	/**
	 * <p>快速允许（allowedFast）Piece长度：{@value}</p>
	 */
	private static final int ALLOWED_FAST_K = 10;
	/**
	 * <p>快速允许（allowedFast）IP Mask：{@value}</p>
	 */
	private static final int ALLOWED_FAST_IP_MASK = 0xFFFFFF00;
	/**
	 * <p>快速允许（allowedFast）循环次数：{@value}</p>
	 */
	private static final int ALLOWED_FAST_LOOP_LENGTH = 5;

	/**
	 * <p>读取IP和端口</p>
	 * 
	 * @param bytes 数据
	 * 
	 * @return IP=端口
	 * 
	 * @see #read(ByteBuffer)
	 */
	public static final Map<String, Integer> read(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		return read(ByteBuffer.wrap(bytes));
	}
	
	/**
	 * <p>读取IP和端口</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return IP=端口
	 */
	public static final Map<String, Integer> read(ByteBuffer buffer) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.remaining() >= SystemConfig.IP_PORT_LENGTH) {
			final String ip = NetUtils.intToIP(buffer.getInt());
			final int port = NetUtils.portToInt(buffer.getShort());
			data.put(ip, port);
		}
		return data;
	}
	
	/**
	 * <p>读取IP和端口</p>
	 * 
	 * @param object 数据
	 * 
	 * @return IP=端口
	 */
	public static final Map<String, Integer> read(Object object) {
		Map<String, Integer> peers;
		if(object instanceof byte[] bytes) {
			// compact：紧凑
			peers = PeerUtils.read(bytes);
		} else if(object instanceof ByteBuffer buffer) {
			peers = PeerUtils.read(buffer);
		} else if (object instanceof List<?> list) {
			// compact：地址
			peers = list.stream()
				.filter(Objects::nonNull)
				.map(value -> {
					final Map<?, ?> map = (Map<?, ?>) value;
					return Map.entry(
						BEncodeDecoder.getString(map, "ip"),
						BEncodeDecoder.getInteger(map, "port")
					);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} else {
			peers = new HashMap<>();
			LOGGER.debug("Peer声明消息格式没有适配：{}", object);
		}
		return peers;
	}
	
	/**
	 * <p>计算快速允许Piece索引</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0006.html</p>
	 * 
	 * @param pieceSize Piece数量（种子Piece总量）
	 * @param ipAddress IP地址（Peer地址）
	 * @param infoHash InfoHash
	 * 
	 * @return 快速允许Piece索引
	 */
	public static final int[] allowedFast(int pieceSize, String ipAddress, byte[] infoHash) {
		final int ipValue = NetUtils.ipToInt(ipAddress);
		// IP(4) + InfoHash(20)
		ByteBuffer buffer = ByteBuffer.allocate(24);
		buffer.putInt(ALLOWED_FAST_IP_MASK & ipValue);
		buffer.put(infoHash);
		int size = 0;
		// 选择数据长度
		final int length = Math.min(ALLOWED_FAST_K, pieceSize);
		final int[] seqs = new int[length];
		while(size < length) {
			buffer = ByteBuffer.wrap(DigestUtils.sha1(buffer.array()));
			for (int index = 0; index < ALLOWED_FAST_LOOP_LENGTH && size < length; index++) {
				final int seq = (int) (Integer.toUnsignedLong(buffer.getInt()) % pieceSize);
				if(ArrayUtils.indexOf(seqs, 0, size, seq) <= -1) {
					seqs[size++] = seq;
				}
			}
		}
		return seqs;
	}
	
	/**
	 * <p>HTTP编码（PeerId、InfoHash）</p>
	 * <p>协议链接：https://wiki.theory.org/index.php/BitTorrentSpecification#Tracker_HTTP.2FHTTPS_Protocol</p>
	 * 
	 * @param bytes PeerId、InfoHash
	 * 
	 * @return HTTP编码
	 * 
	 * @see #needEncode(char)
	 */
	public static final String urlEncode(byte[] bytes) {
		char value;
		String valueHex;
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < bytes.length; index++) {
			value = (char) bytes[index];
			if(noneEncode(value)) {
				// 不用编码字符
				builder.append(value);
			} else {
				// 需要编码字符
				builder.append(SymbolConfig.Symbol.PERCENT.toString());
				valueHex = Integer.toHexString(value & 0xFF);
				if(valueHex.length() < 2) {
					builder.append(SymbolConfig.Symbol.ZERO.toString());
				}
				builder.append(valueHex);
			}
		}
		return builder.toString();
	}
	
	/**
	 * <p>判断是否不用编码</p>
	 * <p>不用编码字符：0-9 a-z A-Z . - _ ~</p>
	 * 
	 * @param value 字符
	 * 
	 * @return 是否不用编码
	 */
	private static final boolean noneEncode(char value) {
		return
			(value >= '0' && value <= '9') ||
			(value >= 'a' && value <= 'z') ||
			(value >= 'A' && value <= 'Z') ||
			value == '.' ||
			value == '-' ||
			value == '_' ||
			value == '~';
	}
	
}
