package com.acgist.snail.net.peer.extension;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.MessageType;
import com.acgist.snail.net.peer.MessageType.ExtensionType;
import com.acgist.snail.net.peer.MessageType.UtMetadataType;
import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.utils.BCodeBuilder;
import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 * TODO：大量请求时拒绝请求
 * TODO：消息流水线
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
	private static final String ARG_X = "x";
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final PeerMessageHandler peerMessageHandler;
	private final ExtensionMessageHandler extensionMessageHandler;
	
	public static final UtMetadataMessageHandler newInstance(InfoHash infoHash, PeerSession peerSession, PeerMessageHandler peerMessageHandler, ExtensionMessageHandler extensionMessageHandler) {
		return new UtMetadataMessageHandler(infoHash, peerSession, peerMessageHandler, extensionMessageHandler);
	}
	
	private UtMetadataMessageHandler(InfoHash infoHash, PeerSession peerSession, PeerMessageHandler peerMessageHandler, ExtensionMessageHandler extensionMessageHandler) {
		this.infoHash = infoHash;
		this.peerSession = peerSession;
		this.peerMessageHandler = peerMessageHandler;
		this.extensionMessageHandler = extensionMessageHandler;
	}

	/**
	 * 消息处理
	 */
	public void onMessage(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final Map<String, Object> data = BCodeUtils.d(bytes);
		final Byte typeValue = BCodeUtils.getByte(data, ARG_MSG_TYPE);
		final UtMetadataType type = MessageType.UtMetadataType.valueOf(typeValue);
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
			data(data);
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
			final var request = buildMessage(MessageType.UtMetadataType.request, index);
			pushMessage(utMetadataType(), request);
		}
	}
	
	private void request(Map<String, Object> data) {
		final int piece = BCodeUtils.getInteger(data, ARG_PIECE);
		data(piece);
	}

	public void data(int piece) {
		final byte[] bytes = infoHash.info();
		if(bytes == null) {
			return;
		}
		final int begin = piece * INFO_SLICE_SIZE;
		final int end = begin + INFO_SLICE_SIZE;
		if(begin > bytes.length) {
			return;
		}
		int length = end;
		if(end > bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = new byte[length];
		System.arraycopy(bytes, begin, x, 0, length);
		final var data = buildMessage(MessageType.UtMetadataType.data, piece);
		data.put(ARG_TOTAL_SIZE, infoHash.size());
		data.put(ARG_X, x);
		pushMessage(utMetadataType(), data);
	}

	private void data(Map<String, Object> data) {
		final int piece = BCodeUtils.getInteger(data, ARG_PIECE);
		final byte[] bytes = infoHash.info();
		final int begin = piece * INFO_SLICE_SIZE;
		final int end = begin + INFO_SLICE_SIZE;
		if(begin > bytes.length) {
			return;
		}
		int length = end;
		if(end > bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = BCodeUtils.getBytes(data, ARG_X);
		System.arraycopy(x, 0, bytes, begin, length);
	}
	
	public void reject() {
		final var reject = buildMessage(MessageType.UtMetadataType.reject, 0);
		pushMessage(utMetadataType(), reject);
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
	 * @param type 扩展消息类型：注意客户端和服务的类型不同
	 */
	private void pushMessage(Byte type, Map<String, Object> data) {
		if (type == null) {
			return;
		}
		final BCodeBuilder builder = BCodeBuilder.newInstance();
		final byte[] bytes = builder.build(data).bytes();
		final byte[] pushBytes = extensionMessageHandler.buildMessage(type, bytes);
		peerMessageHandler.pushMessage(MessageType.Type.extension, pushBytes);
	}
	
	/**
	 * 创建消息
	 * @param type UtMetadata类型
	 */
	private Map<String, Object> buildMessage(MessageType.UtMetadataType type, int piece) {
		final Map<String, Object> message = new LinkedHashMap<>();
		message.put(ARG_MSG_TYPE, type.value());
		message.put(ARG_PIECE, piece);
		return message;
	}

}
