package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionTypeGetter;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.PeerConfig.MetadataType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>InfoHash交换种子文件信息（info）</p>
 * <p>Extension for Peers to Send Metadata Files</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0009.html</p>
 * 
 * TODO：大量请求时拒绝请求
 * 
 * @author acgist
 * @since 1.0.0
 */
public class MetadataMessageHandler implements IExtensionMessageHandler, IExtensionTypeGetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMessageHandler.class);
	
	/**
	 * 数据交换块大小：16KB
	 */
	public static final int SLICE_LENGTH = 16 * SystemConfig.ONE_KB;
	/**
	 * piece index：piece索引
	 */
	private static final String ARG_PIECE = "piece";
	/**
	 * {@linkplain MetadataType 消息类型}
	 */
	private static final String ARG_MSG_TYPE = "msg_type";
	/**
	 * Info数据大小
	 */
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
	public void onMessage(ByteBuffer buffer) throws NetException {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final var decoder = BEncodeDecoder.newInstance(bytes);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("metadata消息格式错误：{}", decoder.oddString());
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
		case REQUEST:
			request(decoder);
			break;
		case DATA:
			data(decoder);
			break;
		case REJECT:
			reject(decoder);
			break;
		default:
			LOGGER.info("不支持的metadata消息类型：{}", type);
			break;
		}
	}
	
	@Override
	public Byte extensionType() {
		return this.peerSession.extensionTypeValue(ExtensionType.UT_METADATA);
	}
	
	/**
	 * 发送消息：request
	 */
	public void request() {
		LOGGER.debug("发送metadata消息-request");
		final int size = this.infoHash.size();
		final int messageSize = NumberUtils.ceilDiv(size, SLICE_LENGTH);
		for (int index = 0; index < messageSize; index++) {
			final var request = buildMessage(PeerConfig.MetadataType.REQUEST, index);
			pushMessage(request);
		}
	}
	
	/**
	 * 处理消息：request
	 */
	private void request(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-request");
		final int piece = decoder.getInteger(ARG_PIECE);
		data(piece);
	}

	/**
	 * 发送消息：data
	 * 
	 * @param piece 种子块索引
	 */
	public void data(int piece) {
		LOGGER.debug("发送metadata消息-data");
		final byte[] bytes = this.infoHash.info(); // InfoHash数据
		if(bytes == null) {
			reject();
			return;
		}
		final int begin = piece * SLICE_LENGTH;
		final int end = begin + SLICE_LENGTH;
		if(begin > bytes.length) {
			reject();
			return;
		}
		int length = SLICE_LENGTH;
		if(end >= bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = new byte[length]; // InfoHash块数据
		System.arraycopy(bytes, begin, x, 0, length);
		final var data = buildMessage(PeerConfig.MetadataType.DATA, piece);
		data.put(ARG_TOTAL_SIZE, this.infoHash.size());
		pushMessage(data, x);
	}

	/**
	 * 处理消息：data
	 * 
	 * @param decoder B编码数据
	 */
	private void data(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-data");
		byte[] bytes = this.infoHash.info();
		final int piece = decoder.getInteger(ARG_PIECE);
		// 设置种子infoInfo
		if(bytes == null) {
			final int totalSize = decoder.getInteger(ARG_TOTAL_SIZE);
			bytes = new byte[totalSize];
			this.infoHash.info(bytes);
		}
		final int begin = piece * SLICE_LENGTH;
		final int end = begin + SLICE_LENGTH;
		if(begin > bytes.length) {
			LOGGER.warn("处理metadata消息：数据长度错误（{}-{}）", begin, bytes.length);
			return;
		}
		int length = SLICE_LENGTH;
		if(end >= bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = decoder.oddBytes(); // 获取剩余数据作为块数据
		System.arraycopy(x, 0, bytes, begin, length);
		final byte[] sourceHash = this.infoHash.infoHash();
		final byte[] targetHash = StringUtils.sha1(bytes);
		// 判断hash值是否相等，如果相等表示已经下载完成。完成后保存种子文件。
		if(ArrayUtils.equals(sourceHash, targetHash)) {
			this.torrentSession.saveTorrentFile();
		}
	}
	
	/**
	 * 发送消息：reject
	 */
	public void reject() {
		LOGGER.debug("发送metadata消息-reject");
		final var reject = buildMessage(PeerConfig.MetadataType.REJECT, 0);
		pushMessage(reject);
	}
	
	/**
	 * 处理消息：reject
	 */
	private void reject(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-reject");
	}
	
	/**
	 * 创建消息
	 * 
	 * @param type metadata消息类型
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
		final Byte type = extensionType();
		if (type == null) {
			LOGGER.warn("不支持metadata扩展协议");
			return;
		}
		final var encoder = BEncodeEncoder.newInstance();
		encoder.newMap().put(data).flush();
		if(x != null) {
			encoder.write(x);
		}
		final byte[] bytes = encoder.bytes();
		this.extensionMessageHandler.pushMessage(type, bytes);
	}
	
}
