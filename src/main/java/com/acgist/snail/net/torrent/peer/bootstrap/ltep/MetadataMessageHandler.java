package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
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
 * <p>Extension for Peers to Send Metadata Files</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0009.html</p>
 * <p>InfoHash交换种子{@linkplain Torrent#getInfo() 文件信息}</p>
 * 
 * TODO：大量请求时拒绝请求
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class MetadataMessageHandler extends ExtensionTypeMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMessageHandler.class);
	
	/**
	 * <p>数据交换Slice大小：{@value}</p>
	 */
	public static final int SLICE_LENGTH = 16 * SystemConfig.ONE_KB;
	/**
	 * <p>Slice索引：{@value}</p>
	 */
	private static final String ARG_PIECE = "piece";
	/**
	 * <p>{@linkplain MetadataType 消息类型}：{@value}</p>
	 */
	private static final String ARG_MSG_TYPE = "msg_type";
	/**
	 * <p>InfoHash种子文件数据大小：{@value}</p>
	 */
	private static final String ARG_TOTAL_SIZE = "total_size";
	
	private final InfoHash infoHash;
	private final TorrentSession torrentSession;
	
	private MetadataMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_METADATA, peerSession, extensionMessageHandler);
		this.infoHash = torrentSession.infoHash();
		this.torrentSession = torrentSession;
	}
	
	public static final MetadataMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new MetadataMessageHandler(peerSession, torrentSession, extensionMessageHandler);
	}

	@Override
	public void doMessage(ByteBuffer buffer) throws NetException {
		final var decoder = BEncodeDecoder.newInstance(buffer);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理metadata消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final Byte typeId = decoder.getByte(ARG_MSG_TYPE);
		final MetadataType metadataType = PeerConfig.MetadataType.valueOf(typeId);
		if(metadataType == null) {
			LOGGER.warn("处理metadata消息错误（类型不支持）：{}", typeId);
			return;
		}
		LOGGER.debug("处理metadata消息类型：{}", metadataType);
		switch (metadataType) {
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
			LOGGER.info("处理metadata消息错误（类型未适配）：{}", metadataType);
			break;
		}
	}
	
	/**
	 * <p>发送消息：request</p>
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
	 * <p>处理消息：request</p>
	 * 
	 * @param decoder 消息（B编码解码器）
	 */
	private void request(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-request");
		final int piece = decoder.getInteger(ARG_PIECE);
		data(piece);
	}

	/**
	 * <p>发送消息：data</p>
	 * 
	 * @param piece Slice索引
	 */
	public void data(int piece) {
		LOGGER.debug("发送metadata消息-data：{}", piece);
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
		final byte[] x = new byte[length]; // Slice数据
		System.arraycopy(bytes, begin, x, 0, length);
		final var data = buildMessage(PeerConfig.MetadataType.DATA, piece);
		data.put(ARG_TOTAL_SIZE, this.infoHash.size());
		pushMessage(data, x);
	}

	/**
	 * <p>处理消息：data</p>
	 * 
	 * @param decoder 消息（B编码解码器）
	 */
	private void data(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-data");
		byte[] bytes = this.infoHash.info();
		final int piece = decoder.getInteger(ARG_PIECE);
		// 设置种子Info
		if(bytes == null) {
			final int totalSize = decoder.getInteger(ARG_TOTAL_SIZE);
			bytes = new byte[totalSize];
			this.infoHash.info(bytes);
		}
		final int begin = piece * SLICE_LENGTH;
		final int end = begin + SLICE_LENGTH;
		if(begin > bytes.length) {
			LOGGER.warn("处理metadata消息-data失败（数据长度错误）：{}-{}", begin, bytes.length);
			return;
		}
		int length = SLICE_LENGTH;
		if(end >= bytes.length) {
			length = bytes.length - begin;
		}
		final byte[] x = decoder.oddBytes(); // 剩余数据作为Slice数据
		System.arraycopy(x, 0, bytes, begin, length);
		final byte[] sourceHash = this.infoHash.infoHash();
		final byte[] targetHash = StringUtils.sha1(bytes);
		// 判断Hash值是否相等：相等表示已经下载完成，完成后保存种子文件。
		if(ArrayUtils.equals(sourceHash, targetHash)) {
			this.torrentSession.saveTorrent();
		}
	}
	
	/**
	 * <p>发送消息：reject</p>
	 */
	public void reject() {
		LOGGER.debug("发送metadata消息-reject");
		final var reject = buildMessage(PeerConfig.MetadataType.REJECT, 0);
		pushMessage(reject);
	}
	
	/**
	 * <p>处理消息：reject</p>
	 * 
	 * @param decoder 消息（B编码解码器）
	 */
	private void reject(BEncodeDecoder decoder) {
		LOGGER.debug("处理metadata消息-reject");
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @param type 消息类型
	 * @param piece Slice索引
	 * 
	 * @return 消息
	 */
	private Map<String, Object> buildMessage(PeerConfig.MetadataType type, int piece) {
		final Map<String, Object> message = new LinkedHashMap<>();
		message.put(ARG_MSG_TYPE, type.id());
		message.put(ARG_PIECE, piece);
		return message;
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param data 消息
	 */
	private void pushMessage(Map<String, Object> data) {
		this.pushMessage(data, null);
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param data 消息
	 * @param x Slice数据
	 */
	private void pushMessage(Map<String, Object> data, byte[] x) {
		final var encoder = BEncodeEncoder.newInstance();
		encoder.newMap().put(data).flush();
		if(x != null) {
			encoder.write(x);
		}
		final byte[] bytes = encoder.bytes();
		this.pushMessage(bytes);
	}
	
}
