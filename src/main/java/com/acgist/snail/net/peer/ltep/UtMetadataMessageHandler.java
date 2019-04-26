package com.acgist.snail.net.peer.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.PeerMessageConfig;
import com.acgist.snail.system.config.PeerMessageConfig.ExtensionType;
import com.acgist.snail.system.config.PeerMessageConfig.UtMetadataType;
import com.acgist.snail.utils.NumberUtils;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 * TODO：大量请求时拒绝请求
 */
public class UtMetadataMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtMetadataMessageHandler.class);
	
	/**
	 * 16KB
	 */
	public static final int INFO_SLICE_SIZE = 16 * 1024;
	
	private static final String ARG_MSG_TYPE = "msg_type";
	private static final String ARG_PIECE = "piece";
	private static final String ARG_TOTAL_SIZE = "total_size";
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	private final ExtensionMessageHandler extensionMessageHandler;
	
	public static final UtMetadataMessageHandler newInstance(TorrentSession torrentSession, PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new UtMetadataMessageHandler(torrentSession, peerSession, extensionMessageHandler);
	}
	
	private UtMetadataMessageHandler(TorrentSession torrentSession, PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}

	/**
	 * 消息处理
	 */
	public void onMessage(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> data = decoder.mustMap();
		final Byte typeValue = BCodeDecoder.getByte(data, ARG_MSG_TYPE);
		final UtMetadataType type = PeerMessageConfig.UtMetadataType.valueOf(typeValue);
		if(type == null) {
			LOGGER.warn("不支持的UtMetadata消息类型：{}", typeValue);
			return;
		}
		LOGGER.debug("UtMetadata消息类型：{}", type);
		switch (type) {
		case request:
			request(data);
			break;
		case data:
			data(data, decoder);
			break;
		case reject:
			reject(data);
			break;
		}
	}
	
	public void request() {
		final int size = infoHash.size();
		final int messageSize = NumberUtils.divideUp(size, INFO_SLICE_SIZE);
		for (int index = 0; index < messageSize; index++) {
			final var request = buildMessage(PeerMessageConfig.UtMetadataType.request, index);
			pushMessage(request);
		}
	}
	
	private void request(Map<String, Object> data) {
		final int piece = BCodeDecoder.getInteger(data, ARG_PIECE);
		data(piece);
	}

	public void data(int piece) {
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
		final var data = buildMessage(PeerMessageConfig.UtMetadataType.data, piece);
		data.put(ARG_TOTAL_SIZE, infoHash.size());
		pushMessage(data, x);
	}

	private void data(Map<String, Object> data, BCodeDecoder decoder) {
		boolean over = false; // 下载完成
		final int piece = BCodeDecoder.getInteger(data, ARG_PIECE);
		final byte[] bytes = infoHash.info();
		final int begin = piece * INFO_SLICE_SIZE;
		final int end = begin + INFO_SLICE_SIZE;
		if(begin > bytes.length) {
			return;
		}
		int length = INFO_SLICE_SIZE;
		if(end >= bytes.length) {
			over = true;
			length = bytes.length - begin;
		}
		final byte[] x = decoder.oddBytes();
		System.arraycopy(x, 0, bytes, begin, length);
		if(over) {
			this.torrentSession.saveTorrentFile();
		}
	}
	
	public void reject() {
		final var reject = buildMessage(PeerMessageConfig.UtMetadataType.reject, 0);
		pushMessage(reject);
	}
	
	private void reject(Map<String, Object> data) {
		LOGGER.warn("UtMetadata被拒绝");
	}
	
	/**
	 * 客户端的消息类型
	 */
	private Byte utMetadataType() {
		return peerSession.extensionTypeValue(ExtensionType.ut_metadata);
	}
	
	/**
	 * 创建消息
	 * @param type UtMetadata类型
	 */
	private Map<String, Object> buildMessage(PeerMessageConfig.UtMetadataType type, int piece) {
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
		final Byte type = utMetadataType(); // 扩展消息类型
		if (type == null) {
			LOGGER.warn("不支持UtMetadata扩展协议");
			return;
		}
		int length = 0;
		final BCodeEncoder encoder = BCodeEncoder.newInstance();
		final byte[] dataBytes = encoder.build(data).bytes();
		length += dataBytes.length;
		if(x != null) {
			length += x.length;
		}
		final byte[] bytes = new byte[length];
		System.arraycopy(dataBytes, 0, bytes, 0, dataBytes.length);
		if(x != null) {
			System.arraycopy(x, 0, bytes, dataBytes.length, x.length);
		}
		extensionMessageHandler.pushMessage(type, bytes);
	}
	
}
