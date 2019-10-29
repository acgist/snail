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
import com.acgist.snail.system.config.CryptConfig;
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
	 * 明文
	 */
	private static final int PLAINTEXT = 0;
	/**
	 * 加密
	 */
	private static final int ENCRYPT = 0;
	
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
	 * <p>任务已经完成：只上传不下载</p>
	 * <p>协议链接：http://bittorrent.org/beps/bep_0021.html</p>
	 */
	public static final String EX_UPLOAD_ONLY = "upload_only";
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
		this.holepunchExtensionMessageHnadler = HolepunchMessageHnadler.newInstance(this.peerSession, this.torrentSession, this);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) throws NetException {
		final byte typeId = buffer.get();
		final ExtensionType extensionType = ExtensionType.valueOf(typeId);
		if(extensionType == null) {
			LOGGER.warn("扩展消息错误（类型不支持）：{}", typeId);
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
			LOGGER.info("扩展消息错误（类型未适配）：{}", extensionType);
			break;
		}
	}
	
	/**
	 * 发送握手消息
	 */
	public void handshake() {
		LOGGER.debug("发送扩展消息-握手");
		this.handshake = true;
		final Map<String, Object> data = new LinkedHashMap<>();
		final Map<String, Object> supportTypes = new LinkedHashMap<>();
		for (var type : PeerConfig.ExtensionType.values()) {
			if(type.support() && type.notice()) {
				supportTypes.put(type.value(), type.id());
			}
		}
		data.put(EX_M, supportTypes); // 扩展协议
		data.put(EX_V, SystemConfig.getNameEnAndVersion()); // 客户端信息（名称、版本）
		data.put(EX_P, SystemConfig.getTorrentPortExt()); // 外网监听TCP端口
		// 本机IP地址：客户端自动获取
//		final String yourip = SystemConfig.getExternalIpAddress();
//		if(StringUtils.isNotEmpty(yourip)) {
//			final ByteBuffer youripBuffer = ByteBuffer.allocate(4);
//			youripBuffer.putInt(NetUtils.encodeIpToInt(yourip));
//			data.put(EX_YOURIP, youripBuffer.array());
//		}
		data.put(EX_REQQ, 255);
		if(PeerConfig.ExtensionType.UT_PEX.notice()) {
			// 偏爱加密
			data.put(EX_E, CryptConfig.STRATEGY.crypt() ? ENCRYPT : PLAINTEXT);
		}
		if(PeerConfig.ExtensionType.UT_METADATA.notice()) {
			final int metadataSize = this.infoHash.size();
			if(metadataSize > 0) {
				data.put(EX_METADATA_SIZE, metadataSize); // 种子InfoHash数据长度
			}
		}
		// 任务已经完成只上传不下载
		if(this.torrentSession.completed()) {
			data.put(EX_UPLOAD_ONLY, 1);
		}
		this.pushMessage(ExtensionType.HANDSHAKE.id(), BEncodeEncoder.encodeMap(data));
	}

	/**
	 * 处理握手消息
	 */
	private void handshake(ByteBuffer buffer) throws PacketSizeException {
		LOGGER.debug("处理扩展消息-握手");
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final var decoder = BEncodeDecoder.newInstance(bytes);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("扩展消息-握手处理失败（格式）：{}", decoder.oddString());
			return;
		}
		// 获取端口
		final Long port = decoder.getLong(EX_P);
		if(port != null) {
			final Integer oldPort = this.peerSession.peerPort();
			if(oldPort != null && oldPort.intValue() != port.intValue()) {
				LOGGER.debug("扩展消息-握手（端口不一致）：{}-{}", oldPort, port);
			}
			this.peerSession.peerPort(port.intValue());
		}
		// 偏爱加密
		final Long encrypt = decoder.getLong(EX_E);
		if(encrypt != null && encrypt.intValue() == ENCRYPT) {
			this.peerSession.flags(PeerConfig.PEX_PREFER_ENCRYPTION);
		}
		// 获取种子InfoHash大小
		final Long metadataSize = decoder.getLong(EX_METADATA_SIZE);
		if(metadataSize != null && this.infoHash.size() == 0) {
			this.infoHash.size(metadataSize.intValue());
		}
		// 支持的扩展协议：key（扩展协议名称）=value（扩展协议标识）
		final Map<String, Object> supportTypes = decoder.getMap(EX_M);
		if(CollectionUtils.isNotEmpty(supportTypes)) {
			supportTypes.entrySet().forEach(entry -> {
				final Long typeId = (Long) entry.getValue();
				final String typeValue = (String) entry.getKey();
				final PeerConfig.ExtensionType extensionType = PeerConfig.ExtensionType.valueOfValue(typeValue);
				this.peerSession.flags(extensionType);
				if(extensionType != null && extensionType.support()) {
					LOGGER.debug("扩展协议（添加）：{}-{}", extensionType, typeId);
					this.peerSession.addExtensionType(extensionType, typeId.byteValue());
				} else {
					LOGGER.debug("扩展协议（不支持）：{}-{}", typeValue, typeId);
				}
			});
		}
		if(!this.handshake) {
			handshake();
		}
		if(this.torrentSession.action() == Action.MAGNET) {
			metadata();
		}
	}
	
	/**
	 * 发送pex消息
	 */
	public void pex(byte[] bytes) {
		if(this.peerExchangeMessageHandler.supportExtensionType()) {
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
		if(this.metadataMessageHandler.supportExtensionType()) {
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
		if(this.holepunchExtensionMessageHnadler.supportExtensionType()) {
			this.holepunchExtensionMessageHnadler.holepunch(host, port);
		}
	}
	
	/**
	 * 发送holepunch连接消息
	 */
	public void holepunchConnect(String host, Integer port) {
		if(this.holepunchExtensionMessageHnadler.supportExtensionType()) {
			this.holepunchExtensionMessageHnadler.connect(host, port);
		}
	}
	
	/**
	 * 处理holepunch消息
	 */
	private void holepunch(ByteBuffer buffer) {
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
