package com.acgist.snail.net.peer.extension;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.MessageType;
import com.acgist.snail.net.peer.MessageType.Action;
import com.acgist.snail.net.peer.MessageType.ExtensionType;
import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.BCodeBuilder;
import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.CollectionUtils;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 * http://www.bittorrent.org/beps/bep_0010.html
 * http://www.bittorrent.org/beps/bep_0011.html
 * https://www.cnblogs.com/LittleHann/p/6180296.html
 */
public class ExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);

	private final InfoHash infoHash;
	private final PeerSession peerSession;
//	private final TorrentSession torrentSession;
	private final PeerMessageHandler peerMessageHandler;
	private final UtMetadataMessageHandler utMetadataMessageHandler;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerMessageHandler peerMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerMessageHandler);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerMessageHandler peerMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
//		this.torrentSession = torrentSession;
		this.peerMessageHandler = peerMessageHandler;
		this.utMetadataMessageHandler = UtMetadataMessageHandler.newInstance(this.infoHash, this.peerSession, peerMessageHandler, this);
	}
	
	/**
	 * 处理扩展消息
	 */
	public void onMessage(ByteBuffer buffer) {
		final byte typeValue = buffer.get();
		final ExtensionType extensionType = ExtensionType.valueOf(typeValue);
		if(extensionType == null) {
			LOGGER.error("不支持扩展类型：{}", typeValue);
			return;
		}
		LOGGER.debug("扩展消息类型：{}", extensionType);
		switch (extensionType) {
		case handshake:
			handshake(buffer);
			break;
		case ut_pex:
			break;
		case ut_metadata:
			utMetadata(buffer);
			break;
		}
	}
	
	/**
	 * 扩展消息握手
	 */
	public void handshake() {
		final Map<String, Object> data = new LinkedHashMap<>();
//		data.put("e", 0); // ut_pex：加密
		final Map<String, Object> supportType = new LinkedHashMap<>();
		for (var type : MessageType.ExtensionType.values()) {
			if(type.support()) {
				supportType.put(type.name(), type.value());
			}
		}
		data.put("m", supportType); // 扩展协议以及编号
		final int size = this.infoHash.size();
		if(size > 0) {
//			data.put("metadata_size", size); // 种子info数据长度
		}
		data.put("v", SystemConfig.getNameAndVersion()); // 客户端信息（名称、版本）
//		final String ipAddress = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ipAddress)) {
//			data.put("p", SystemConfig.getPeerPort()); // 本机监听TCP端口
//			ByteBuffer ipBuffer = ByteBuffer.allocate(4);
//			ipBuffer.putInt(NetUtils.ipToInt(UpnpService.getInstance().externalIpAddress()));
//			data.put("yourip", ipBuffer.array()); // 本机的IP地址
//		}
		data.put("reqq", 255); // TODO：详细意思未知
		final BCodeBuilder builder = BCodeBuilder.newInstance();
		final byte[] bytes = buildMessage(ExtensionType.handshake.value(), builder.build(data).bytes());
		peerMessageHandler.pushMessage(MessageType.Type.extension, bytes);
	}
	
	/**
	 * 扩展消息握手
	 */
	private void handshake(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final Map<String, Object> data = BCodeUtils.d(bytes);
		final Object size = data.get("metadata_size");
		if(size != null && this.infoHash.size() == 0) {
			this.infoHash.size((int) size);
		}
		final Map<?, ?> mData = (Map<?, ?>) data.get("m");
		if(CollectionUtils.isNotEmpty(mData)) {
			mData.entrySet().forEach(entry -> {
				final String type = (String) entry.getKey();
				final Long typeValue = (Long) entry.getValue();
				final MessageType.ExtensionType extensionType = MessageType.ExtensionType.valueOfName(type);
				if(extensionType == null) {
					LOGGER.debug("不支持的扩展协议：{}", type);
				} else {
					peerSession.addExtensionType(extensionType, typeValue.byteValue());
				}
			});
		}
		if(peerMessageHandler.isServer()) {
			handshake();
		}
		if (peerMessageHandler.action() == Action.torrent) { // 下载种子
			if(peerSession.support(ExtensionType.ut_metadata)) {
				utMetadataMessageHandler.request();
			}
		}
	}
	
	/**
	 * 种子信息
	 */
	private void utMetadata(ByteBuffer buffer) {
		utMetadataMessageHandler.onMessage(buffer);
	}

	/**
	 * 数据打包
	 * @param type 扩展类型
	 * @param bytes 扩展数据
	 */
	public byte[] buildMessage(byte type, byte[] bytes) {
		final ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.put(type);
		buffer.put(bytes);
		return buffer.array();
	}
	
}
