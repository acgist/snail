package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.Action;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>LTEP（Libtorrent Extension Protocol）扩展协议</p>
 * <p>Extension Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0010.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ExtensionMessageHandler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);
	
	/**
	 * 扩展协议信息
	 */
	public static final String EX_M = "m";
	/**
	 * 软件信息（名称和版本）
	 */
	public static final String EX_V = "v";
	/**
	 * 端口
	 */
	public static final String EX_P = "p";
	/**
	 * PEX：加密
	 */
	public static final String EX_E = "e";
	/**
	 * 未知含义
	 * 
	 * TODO：了解
	 */
	public static final String EX_REQQ = "reqq";
	/**
	 * IPv4地址
	 */
	public static final String EX_IPV4 = "ipv4";
	/**
	 * IPv6地址
	 */
	public static final String EX_IPV6 = "ipv6";
	/**
	 * 本地地址（外网）：不设置自动获取
	 */
	public static final String EX_YOURIP = "yourip";
	/**
	 * ut_metadata：种子info数据大小
	 */
	public static final String EX_METADATA_SIZE = "metadata_size";

	/**
	 * 是否已经握手
	 */
	private volatile boolean handshake = false;
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final MetadataMessageHandler metadataMessageHandler;
	private final PeerExchangeMessageHandler peerExchangeMessageHandler;
	private final HolepunchMessageHnadler holepunchExtensionMessageHnadler;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.metadataMessageHandler = MetadataMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.peerExchangeMessageHandler = PeerExchangeMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.holepunchExtensionMessageHnadler = HolepunchMessageHnadler.newInstance(this.peerSession, this);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) throws NetException {
		final byte typeValue = buffer.get();
		final ExtensionType extensionType = ExtensionType.valueOf(typeValue);
		if(extensionType == null) {
			LOGGER.warn("不支持的扩展消息类型：{}", typeValue);
			return;
		}
		LOGGER.debug("扩展消息类型：{}", extensionType);
		switch (extensionType) {
		case HANDSHAKE:
			handshake(buffer);
			break;
		case UT_PEX:
			pex(buffer);
			break;
		case UT_METADATA:
			metadata(buffer);
			break;
		case UT_HOLEPUNCH:
			holepunch(buffer);
			break;
		default:
			LOGGER.info("不支持的扩展消息类型：{}", extensionType);
			break;
		}
	}
	
	/**
	 * 扩展握手消息
	 */
	public void handshake() {
		LOGGER.debug("发送扩展握手消息");
		this.handshake = true;
		final Map<String, Object> data = new LinkedHashMap<>();
		final Map<String, Object> supportType = new LinkedHashMap<>();
		for (var type : PeerConfig.ExtensionType.values()) {
			if(type.notice()) {
				supportType.put(type.key(), type.value());
			}
		}
		data.put(EX_M, supportType); // 扩展协议以及编号
		data.put(EX_V, SystemConfig.getNameEnAndVersion()); // 客户端信息（名称、版本）
		data.put(EX_P, SystemConfig.getTorrentPortExt()); // 外网监听TCP端口
		// 本机IP地址，客户端自动获取。
//		final String ip = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ip)) {
//			final ByteBuffer youripBuffer = ByteBuffer.allocate(4);
//			youripBuffer.putInt(NetUtils.decodeIpToInt(ip));
//			data.put(EX_YOURIP, youripBuffer.array());
//		}
		data.put(EX_REQQ, 255);
		if(PeerConfig.ExtensionType.UT_PEX.notice()) {
			// TODO：使用CryptConfig配置
			data.put(EX_E, 0); // pex：加密
		}
		if(PeerConfig.ExtensionType.UT_METADATA.notice()) {
			final int metadataSize = this.infoHash.size();
			if(metadataSize > 0) {
				data.put(EX_METADATA_SIZE, metadataSize); // 种子InfoHash数据长度
			}
		}
		this.pushMessage(ExtensionType.HANDSHAKE.value(), BEncodeEncoder.encodeMap(data));
	}

	/**
	 * 处理握手消息
	 */
	private void handshake(ByteBuffer buffer) throws PacketSizeException {
		LOGGER.debug("处理扩展握手消息");
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final var decoder = BEncodeDecoder.newInstance(bytes);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("扩展握手消息格式错误：{}", decoder.oddString());
			return;
		}
		// 获取端口
		final Long port = decoder.getLong(EX_P);
		if(port != null) {
			final Integer oldPort = this.peerSession.peerPort();
			if(oldPort != null && oldPort.intValue() != port.intValue()) {
				LOGGER.debug("Peer扩展握手获取端口和原始端口不一致：{}-{}", oldPort, port);
			}
			this.peerSession.peerPort(port.intValue());
		}
		// 获取种子InfoHash大小
		final Long metadataSize = decoder.getLong(EX_METADATA_SIZE);
		if(metadataSize != null && this.infoHash.size() == 0) {
			this.infoHash.size(metadataSize.intValue());
		}
		// 支持的扩展协议：key=扩展消息标识（数字）
		final Map<String, Object> mData = decoder.getMap(EX_M);
		if(CollectionUtils.isNotEmpty(mData)) {
			mData.entrySet().forEach(entry -> {
				final String type = (String) entry.getKey();
				final Long typeValue = (Long) entry.getValue();
				final PeerConfig.ExtensionType extensionType = PeerConfig.ExtensionType.valueOfKey(type);
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
		if(this.torrentSession.action() == Action.magnet) {
			metadata();
		}
	}
	
	/**
	 * 发送pex消息
	 */
	public void pex(byte[] bytes) {
		if(this.peerSession.supportExtensionType(ExtensionType.UT_PEX)) {
			this.peerExchangeMessageHandler.pex(bytes);
		}
	}
	
	/**
	 * 处理pex消息
	 */
	private void pex(ByteBuffer buffer) throws NetException {
		this.peerExchangeMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送metadata消息</p>
	 */
	public void metadata() {
		if(this.peerSession.supportExtensionType(ExtensionType.UT_METADATA)) {
			this.metadataMessageHandler.request();
		}
	}
	
	/**
	 * 处理metadata消息
	 */
	private void metadata(ByteBuffer buffer) throws NetException {
		this.metadataMessageHandler.onMessage(buffer);
	}
	
	/**
	 * 发送holepunch消息
	 */
	public void holepunch(String host, Integer port) {
		this.holepunchExtensionMessageHnadler.holepunch(host, port);
	}
	
	/**
	 * 处理holepunch消息
	 */
	public void holepunch(ByteBuffer buffer) {
		this.holepunchExtensionMessageHnadler.onMessage(buffer);
	}

	/**
	 * 创建扩展消息
	 * 
	 * @param type 扩展类型
	 * @param bytes 扩展数据
	 */
	private byte[] buildMessage(byte type, byte[] bytes) {
		final byte[] message = new byte[bytes.length + 1];
		message[0] = type; // 扩展消息类型
		System.arraycopy(bytes, 0, message, 1, bytes.length);
		return message;
	}
	
	/**
	 * 发送扩展消息
	 * 
	 * @param type 扩展消息类型：需要和Peer的标记一致
	 */
	public void pushMessage(byte type, byte[] bytes) {
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.EXTENSION, buildMessage(type, bytes));
	}

}
