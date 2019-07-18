package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.PeerConfig.MetadataType;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Extension for Peers to Send Metadata Files</p>
 * <p>infoHash交换种子文件信息。</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0009.html</p>
 * TODO：大量请求时拒绝请求
 * 
 * @author acgist
 * @since 1.0.0
 */
public class MetadataMessageHandler implements IExtensionMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMessageHandler.class);
	
	/**
	 * 数据交换每块大小：16KB
	 */
	public static final int INFO_SLICE_SIZE = 16 * 1024;
	
	private static final String ARG_PIECE = "piece";
	private static final String ARG_MSG_TYPE = "msg_type";
	private static final String ARG_TOTAL_SIZE = "total_size";
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final ExtensionMessageHandler extensionMessageHandler;
	
	private MetadataMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public static final MetadataMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new MetadataMessageHandler(peerSession, torrentSession, extensionMessageHandler);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(bytes);
		final Map<String, Object> map = decoder.nextMap();
		if(map == null) {
			LOGGER.warn("metadata消息格式错误：{}", decoder.obbString());
			return;
		}
		final Byte typeValue = decoder.getByte(ARG_MSG_TYPE);
		final MetadataType type = PeerConfig.MetadataType.valueOf(typeValue);
		if(type == null) {
			LOGGER.warn("不支持的metadata消息类型：{}", typeValue);
			return;
		}
		LOGGER.debug("metadata消息类型：{}", type);
		switch (type) {
		case request:
			request(decoder);
			break;
		case data:
			data(decoder);
			break;
		case reject:
			reject(decoder);
			break;
		}
	}
	
	/**
	 * 发出请求：request
	 */
	public void request() {
		LOGGER.debug("发送metadata消息-request");
		final int size = this.infoHash.size();
		final int messageSize = NumberUtils.divideUp(size, INFO_SLICE_SIZE);
		for (int index = 0; index < messageSize; index++) {
			final var request = buildMessage(PeerConfig.MetadataType.request, index);
			pushMessage(request);
		}
	}
	
	/**
	 * 处理请求：request
	 */
	private void request(BEncodeDecoder decoder) {
		LOGGER.debug("收到metadata消息-request");
		final int piece = decoder.getInteger(ARG_PIECE);
		data(piece);
	}

	/**
	 * 发出请求：data
	 * 
	 * @param piece 种子块索引
	 */
	public void data(int piece) {
		LOGGER.debug("发送metadata消息-data");
		final byte[] bytes = infoHash.info();
		if(bytes == null) {
			reject();
			return;
		}
		final int begin = piece * INFO_SLICE_SIZE;
		final int end = begin + INFO_SLICE_SIZE;
		if(begin > bytes.length) {
			reject();
			return;
		}
		int length = INFO_SLICE_SIZE;
		if(end >= bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = new byte[length];
		System.arraycopy(bytes, begin, x, 0, length);
		final var data = buildMessage(PeerConfig.MetadataType.data, piece);
		data.put(ARG_TOTAL_SIZE, this.infoHash.size());
		pushMessage(data, x);
	}

	/**
	 * 处理请求：data
	 * 
	 * @param data 请求数据
	 * @param decoder B编码数据
	 */
	private void data(BEncodeDecoder decoder) {
		LOGGER.debug("收到metadata消息-data");
		byte[] bytes = this.infoHash.info();
		final int piece = decoder.getInteger(ARG_PIECE);
		if(bytes == null) { // 设置种子info
			final int totalSize = decoder.getInteger(ARG_TOTAL_SIZE);
			bytes = new byte[totalSize];
			this.infoHash.info(bytes);
		}
		final int begin = piece * INFO_SLICE_SIZE;
		final int end = begin + INFO_SLICE_SIZE;
		if(begin > bytes.length) {
			return;
		}
		int length = INFO_SLICE_SIZE;
		if(end >= bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = decoder.oddBytes();
		System.arraycopy(x, 0, bytes, begin, length);
		final byte[] sourceHash = this.infoHash.infoHash();
		final byte[] targetHash = StringUtils.sha1(bytes);
		if(ArrayUtils.equals(sourceHash, targetHash)) {
			this.torrentSession.saveTorrentFile();
		}
	}
	
	/**
	 * 发出请求：reject
	 */
	public void reject() {
		LOGGER.debug("发送metadata消息-reject");
		final var reject = buildMessage(PeerConfig.MetadataType.reject, 0);
		pushMessage(reject);
	}
	
	/**
	 * 处理请求：reject
	 */
	private void reject(BEncodeDecoder decoder) {
		LOGGER.debug("收到metadata消息-reject");
	}
	
	/**
	 * 客户端的消息类型
	 */
	private Byte metadataType() {
		return this.peerSession.extensionTypeValue(ExtensionType.ut_metadata);
	}
	
	/**
	 * 创建消息
	 * 
	 * @param type metadata类型
	 */
	private Map<String, Object> buildMessage(PeerConfig.MetadataType type, int piece) {
		final Map<String, Object> message = new LinkedHashMap<>();
		message.put(ARG_MSG_TYPE, type.value());
		message.put(ARG_PIECE, piece);
		return message;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(Map<String, Object> data) {
		this.pushMessage(data, null);
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(Map<String, Object> data, byte[] x) {
		final Byte type = metadataType(); // 扩展消息类型
		if (type == null) {
			LOGGER.warn("不支持metadata扩展协议");
			return;
		}
		final BEncodeEncoder encoder = BEncodeEncoder.newInstance().newMap();
		encoder.put(data).flush();
		if(x != null) {
			encoder.build(x);
		}
		final byte[] bytes = encoder.bytes();
		this.extensionMessageHandler.pushMessage(type, bytes);
	}
	
}
