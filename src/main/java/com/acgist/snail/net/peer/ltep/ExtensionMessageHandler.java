package com.acgist.snail.net.peer.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.PeerMessageConfig;
import com.acgist.snail.system.config.PeerMessageConfig.Action;
import com.acgist.snail.system.config.PeerMessageConfig.ExtensionType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;

/**
 * LTEP（Libtorrent Extension Protocol）扩展协议
 * http://www.bittorrent.org/beps/bep_0009.html
 * http://www.bittorrent.org/beps/bep_0010.html
 * http://www.bittorrent.org/beps/bep_0011.html
 * https://www.cnblogs.com/LittleHann/p/6180296.html
 */
public class ExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);
	
	public static final String EX_M = "m"; // 扩展协议信息
	public static final String EX_V = "v"; // 版本
	public static final String EX_P = "p"; // 端口
	public static final String EX_REQQ = "reqq"; // 含义：未知：TODO：了解清楚
	public static final String EX_YOURIP = "yourip"; // 地址
	
	public static final String EX_E = "e"; // ut_pex：加密
	
	public static final String EX_METADATA_SIZE = "metadata_size"; // ut_metadata：种子info数据大小

	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	private final PeerMessageHandler peerMessageHandler;
	private final UtMetadataMessageHandler utMetadataMessageHandler;
	private final UtPeerExchangeMessageHandler utPeerExchangeMessageHandler;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerMessageHandler peerMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerMessageHandler);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerMessageHandler peerMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerMessageHandler = peerMessageHandler;
		this.utMetadataMessageHandler = UtMetadataMessageHandler.newInstance(this.torrentSession, this.peerSession, this.peerMessageHandler, this);
		this.utPeerExchangeMessageHandler = UtPeerExchangeMessageHandler.newInstance(this.torrentSession, this.peerSession, this.peerMessageHandler, this);
	}
	
	/**
	 * 处理扩展消息
	 */
	public void onMessage(ByteBuffer buffer) {
		final byte typeValue = buffer.get();
		final ExtensionType extensionType = ExtensionType.valueOf(typeValue);
		if(extensionType == null) {
			LOGGER.warn("不支持扩展类型：{}", typeValue);
			return;
		}
		LOGGER.debug("扩展消息类型：{}", extensionType);
		switch (extensionType) {
		case handshake:
			handshake(buffer);
			break;
		case ut_pex:
			utPex(buffer);
			break;
		case ut_metadata:
			utMetadata(buffer);
			break;
		case ut_holepunch:
			break;
		}
	}
	
	/**
	 * 扩展消息握手
	 */
	public void handshake() {
		final Map<String, Object> data = new LinkedHashMap<>();
		final Map<String, Object> supportType = new LinkedHashMap<>();
		for (var type : PeerMessageConfig.ExtensionType.values()) {
			if(type.notice()) {
				supportType.put(type.name(), type.value());
			}
		}
		data.put(EX_M, supportType); // 扩展协议以及编号
		data.put(EX_V, SystemConfig.getNameAndVersion()); // 客户端信息（名称、版本）
		data.put(EX_P, SystemConfig.getPeerPort()); // 本机监听TCP端口
		// 客户端自动获取
//		final String ipAddress = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ipAddress)) {
//			final ByteBuffer youripBuffer = ByteBuffer.allocate(4);
//			youripBuffer.putInt(NetUtils.decodeIpToInt(UpnpService.getInstance().externalIpAddress()));
//			data.put(EX_YOURIP, youripBuffer.array()); // 本机的IP地址
//		}
		data.put(EX_REQQ, 255);
		if(PeerMessageConfig.ExtensionType.ut_pex.notice()) {
			data.put(EX_E, 0); // ut_pex：加密
		}
		if(PeerMessageConfig.ExtensionType.ut_metadata.notice()) {
			final int size = this.infoHash.size();
			if(size > 0) {
				data.put(EX_METADATA_SIZE, size); // 种子info数据长度
			}
		}
		final BCodeEncoder encoder = BCodeEncoder.newInstance();
		final byte[] bytes = buildMessage(ExtensionType.handshake.value(), encoder.build(data).bytes());
		peerMessageHandler.pushMessage(PeerMessageConfig.Type.extension, bytes);
	}

	/**
	 * 扩展消息握手
	 */
	private void handshake(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> data = decoder.mustMap();
		final Object port = data.get(EX_P);
		if(port != null && peerSession.port() == null) { // 获取端口
			peerSession.port(((Long) port).intValue());
		}
		final Object size = data.get(EX_METADATA_SIZE);
		if(size != null && this.infoHash.size() == 0) { // 获取种子info大小
			this.infoHash.size((int) size);
		}
		final Map<?, ?> mData = (Map<?, ?>) data.get(EX_M);
		if(CollectionUtils.isNotEmpty(mData)) {
			mData.entrySet().forEach(entry -> {
				final String type = (String) entry.getKey();
				final Long typeValue = (Long) entry.getValue();
				final PeerMessageConfig.ExtensionType extensionType = PeerMessageConfig.ExtensionType.valueOfName(type);
				if(extensionType == null) {
					LOGGER.debug("不支持的扩展协议：{}-{}", type, typeValue);
				} else {
					LOGGER.debug("添加扩展协议：{}-{}", extensionType, typeValue);
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
	 * ut_pex
	 */
	private void utPex(ByteBuffer buffer) {
		utPeerExchangeMessageHandler.onMessage(buffer);
	}
	
	/**
	 * ut_metadata
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
