package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.Action;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>LTEP（Libtorrent Extension Protocol）扩展协议</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0009.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0010.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);
	
	private volatile boolean handshake = false; // 是否握手
	
	public static final String EX_M = "m"; // 扩展协议信息
	public static final String EX_V = "v"; // 版本
	public static final String EX_P = "p"; // 端口
	public static final String EX_REQQ = "reqq"; // 含义：未知：TODO：了解清楚
	public static final String EX_YOURIP = "yourip"; // 地址
	
	public static final String EX_E = "e"; // Pex：加密
	
	public static final String EX_METADATA_SIZE = "metadata_size"; // ut_metadata：种子info数据大小

	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final MetadataMessageHandler metadataMessageHandler;
	private final PeerExchangeMessageHandler peerExchangeMessageHandler;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.metadataMessageHandler = MetadataMessageHandler.newInstance(this.torrentSession, this.peerSession, this);
		this.peerExchangeMessageHandler = PeerExchangeMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
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
			pex(buffer);
			break;
		case ut_metadata:
			metadata(buffer);
			break;
		case ut_holepunch:
			break;
		}
	}
	
	/**
	 * 扩展握手消息
	 */
	public void handshake() {
		this.handshake = true;
		final Map<String, Object> data = new LinkedHashMap<>();
		final Map<String, Object> supportType = new LinkedHashMap<>();
		for (var type : PeerConfig.ExtensionType.values()) {
			if(type.notice()) {
				supportType.put(type.name(), type.value());
			}
		}
		data.put(EX_M, supportType); // 扩展协议以及编号
		data.put(EX_V, SystemConfig.getNameEnAndVersion()); // 客户端信息（名称、版本）
		data.put(EX_P, SystemConfig.getTorrentPortExt()); // 外网监听TCP端口
		// 客户端自动获取
//		final String ip = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ip)) {
//			final ByteBuffer youripBuffer = ByteBuffer.allocate(4);
//			youripBuffer.putInt(NetUtils.decodeIpToInt(UpnpService.getInstance().externalIpAddress()));
//			data.put(EX_YOURIP, youripBuffer.array()); // 本机的IP地址
//		}
		data.put(EX_REQQ, 255);
		if(PeerConfig.ExtensionType.ut_pex.notice()) {
			data.put(EX_E, 0); // Pex：加密
		}
		if(PeerConfig.ExtensionType.ut_metadata.notice()) {
			final int metadataSize = this.infoHash.size();
			if(metadataSize > 0) {
				data.put(EX_METADATA_SIZE, metadataSize); // 种子info数据长度
			}
		}
		this.pushMessage(ExtensionType.handshake.value(), BCodeEncoder.encodeMap(data));
	}

	/**
	 * 扩展握手消息
	 */
	private void handshake(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> data = decoder.nextMap();
		if(data == null) {
			LOGGER.warn("扩展握手消息格式错误：{}", decoder.obbString());
			return;
		}
		final Long port = decoder.getLong(EX_P);
		if(port != null && this.peerSession.peerPort() == null) { // 获取端口
			this.peerSession.peerPort(port.intValue());
		}
		final Long metadataSize = decoder.getLong(EX_METADATA_SIZE);
		if(metadataSize != null && this.infoHash.size() == 0) { // 获取种子info大小
			this.infoHash.size(metadataSize.intValue());
		}
		final Map<String, Object> mData = decoder.getMap(EX_M);
		if(CollectionUtils.isNotEmpty(mData)) {
			mData.entrySet().forEach(entry -> {
				final String type = (String) entry.getKey();
				final Long typeValue = (Long) entry.getValue();
				final PeerConfig.ExtensionType extensionType = PeerConfig.ExtensionType.valueOfName(type);
				if(extensionType == null) {
					LOGGER.debug("不支持的扩展协议：{}-{}", type, typeValue);
				} else {
					LOGGER.debug("添加扩展协议：{}-{}", extensionType, typeValue);
					this.peerSession.addExtensionType(extensionType, typeValue.byteValue());
				}
			});
		}
		if(!this.handshake) {
			handshake();
		}
		if(this.torrentSession.action() == Action.torrent) {
			downloadTorrent();
		}
	}

	/**
	 * 下载种子
	 */
	public void downloadTorrent() {
		if(this.peerSession.support(ExtensionType.ut_metadata)) {
			this.metadataMessageHandler.request();
		}
	}
	
	/**
	 * pex请求
	 */
	public void exchange(byte[] bytes) {
		this.peerExchangeMessageHandler.exchange(bytes);
	}
	
	/**
	 * pex消息
	 */
	private void pex(ByteBuffer buffer) {
		this.peerExchangeMessageHandler.onMessage(buffer);
	}
	
	/**
	 * metadata消息
	 */
	private void metadata(ByteBuffer buffer) {
		this.metadataMessageHandler.onMessage(buffer);
	}

	/**
	 * 数据打包
	 * 
	 * @param type 扩展类型
	 * @param bytes 扩展数据
	 */
	private byte[] buildMessage(byte type, byte[] bytes) {
		final byte[] message = new byte[bytes.length + 1];
		message[0] = type;
		System.arraycopy(bytes, 0, message, 1, bytes.length);
		return message;
	}
	
	/**
	 * 发送消息
	 * 
	 * @param type 扩展消息类型：需要和Peer的标记一致
	 */
	public void pushMessage(byte type, byte[] bytes) {
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.extension, buildMessage(type, bytes));
	}

}
