package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
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
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>LTEP（Libtorrent Extension Protocol）扩展协议</p>
 * <p>Extension Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0010.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ExtensionMessageHandler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);
	
	/**
	 * 偏爱明文
	 */
	private static final int PLAINTEXT = 0;
	/**
	 * 偏爱加密
	 */
	private static final int ENCRYPT = 1;
	/**
	 * 只上传不下载
	 */
	private static final int UPLOAD_ONLY = 1;
	/**
	 * 默认支持未完成请求数量
	 */
	private static final int DEFAULT_REQQ = 128;
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
	 * 偏爱加密
	 */
	public static final String EX_E = "e";
	/**
	 * 支持未完成请求数量
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
	 * 外网IP地址
	 */
	public static final String EX_YOURIP = "yourip";
	/**
	 * <p>任务已经完成：只上传不下载</p>
	 * <p>协议链接：http://bittorrent.org/beps/bep_0021.html</p>
	 */
	public static final String EX_UPLOAD_ONLY = "upload_only";
	/**
	 * ut_metadata：种子InfoHash数据大小
	 */
	public static final String EX_METADATA_SIZE = "metadata_size";

	/**
	 * 是否已经握手
	 */
	private volatile boolean handshakeSend = false;
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final MetadataMessageHandler metadataMessageHandler;
	private final HolepunchMessageHnadler holepunchMessageHnadler;
	private final PeerExchangeMessageHandler peerExchangeMessageHandler;
	private final DontHaveExtensionMessageHandler dontHaveExtensionMessageHandler;
	private final UploadOnlyExtensionMessageHandler uploadOnlyExtensionMessageHandler;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.metadataMessageHandler = MetadataMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.holepunchMessageHnadler = HolepunchMessageHnadler.newInstance(this.peerSession, this.torrentSession, this);
		this.peerExchangeMessageHandler = PeerExchangeMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.dontHaveExtensionMessageHandler = DontHaveExtensionMessageHandler.newInstance(this.peerSession, this);
		this.uploadOnlyExtensionMessageHandler = UploadOnlyExtensionMessageHandler.newInstance(peerSession, this);
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
		case UPLOAD_ONLY:
			uploadOnly(buffer);
			break;
		case LT_DONTHAVE:
			dontHave(buffer);
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
		this.handshakeSend = true;
		final Map<String, Object> message = new LinkedHashMap<>(); // 扩展消息
		final Map<String, Object> supportTypes = new LinkedHashMap<>(); // 支持的扩展消息类型
		for (var type : PeerConfig.ExtensionType.values()) {
			if(type.support() && type.notice()) {
				supportTypes.put(type.value(), type.id());
			}
		}
		message.put(EX_M, supportTypes); // 扩展协议
		message.put(EX_P, SystemConfig.getTorrentPortExt()); // 外网监听TCP端口
		message.put(EX_V, SystemConfig.getNameEnAndVersion()); // 客户端信息（名称、版本）
		message.put(EX_E, CryptConfig.STRATEGY.crypt() ? ENCRYPT : PLAINTEXT); // 偏爱加密
		// 外网IP地址：TODO：IPv6
		final String yourip = SystemConfig.getExternalIpAddress();
		if(StringUtils.isNotEmpty(yourip)) {
			message.put(EX_YOURIP, NumberUtils.intToBytes(NetUtils.encodeIpToInt(yourip)));
		}
		message.put(EX_REQQ, DEFAULT_REQQ);
		if(PeerConfig.ExtensionType.UT_METADATA.notice()) {
			// 种子InfoHash数据长度
			final int metadataSize = this.infoHash.size();
			if(metadataSize > 0) {
				message.put(EX_METADATA_SIZE, metadataSize);
			}
		}
		// 任务已经完成只上传不下载
		if(this.torrentSession.completed()) {
			message.put(EX_UPLOAD_ONLY, UPLOAD_ONLY);
		}
		this.pushMessage(ExtensionType.HANDSHAKE.id(), BEncodeEncoder.encodeMap(message));
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
			final Integer oldPort = this.peerSession.port();
			if(oldPort == null) {
				this.peerSession.port(port.intValue());
			} else if(oldPort.intValue() != port.intValue()) {
				LOGGER.debug("扩展消息-握手（端口不一致）：{}-{}", oldPort, port);
			}
		}
		// 偏爱加密
		final Long encrypt = decoder.getLong(EX_E);
		if(encrypt != null && encrypt.intValue() == ENCRYPT) {
			this.peerSession.flags(PeerConfig.PEX_PREFER_ENCRYPTION);
		}
		// 种子InfoHash数据长度
		final Long metadataSize = decoder.getLong(EX_METADATA_SIZE);
		if(metadataSize != null && this.infoHash.size() == 0) {
			this.infoHash.size(metadataSize.intValue());
		}
		// 只上传不下载
		final Long uploadOnly = decoder.getLong(EX_UPLOAD_ONLY);
		if(uploadOnly != null && uploadOnly.intValue() == UPLOAD_ONLY) {
			this.peerSession.flags(PeerConfig.PEX_UPLOAD_ONLY);
		}
		// 支持的扩展协议：key（扩展协议名称）=value（扩展协议标识）
		final Map<String, Object> supportTypes = decoder.getMap(EX_M);
		if(CollectionUtils.isNotEmpty(supportTypes)) {
			supportTypes.entrySet().forEach(entry -> {
				final Long typeId = (Long) entry.getValue();
				final String typeValue = (String) entry.getKey();
				final PeerConfig.ExtensionType extensionType = PeerConfig.ExtensionType.valueOfValue(typeValue);
				if(extensionType == PeerConfig.ExtensionType.UT_HOLEPUNCH) {
					this.peerSession.flags(PeerConfig.PEX_HOLEPUNCH);
				}
				if(extensionType != null && extensionType.support()) {
					LOGGER.debug("扩展协议（添加）：{}-{}", extensionType, typeId);
					this.peerSession.addExtensionType(extensionType, typeId.byteValue());
				} else {
					LOGGER.debug("扩展协议（不支持）：{}-{}", typeValue, typeId);
				}
			});
		}
		if(!this.handshakeSend) {
			handshake();
		}
		// 种子文件下载
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
	 * 发送metadata消息
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
	 * 发送holepunch消息-rendezvous
	 */
	public void holepunchRendezvous(String host, Integer port) {
		if(this.holepunchMessageHnadler.supportExtensionType()) {
			this.holepunchMessageHnadler.rendezvous(host, port);
		}
	}
	
	/**
	 * 发送holepunch消息-connect
	 */
	public void holepunchConnect(String host, Integer port) {
		if(this.holepunchMessageHnadler.supportExtensionType()) {
			this.holepunchMessageHnadler.connect(host, port);
		}
	}
	
	/**
	 * 处理holepunch消息
	 */
	private void holepunch(ByteBuffer buffer) throws NetException {
		this.holepunchMessageHnadler.onMessage(buffer);
	}

	/**
	 * 发送uploadOnly消息
	 */
	public void uploadOnly() {
		if(this.uploadOnlyExtensionMessageHandler.supportExtensionType()) {
			this.uploadOnlyExtensionMessageHandler.uploadOnly();
		}
	}
	
	/**
	 * 处理uploadOnly消息
	 */
	private void uploadOnly(ByteBuffer buffer) throws NetException {
		this.uploadOnlyExtensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * 发送dontHave消息
	 */
	public void dontHave(int index) {
		if(this.dontHaveExtensionMessageHandler.supportExtensionType()) {
			this.dontHaveExtensionMessageHandler.dontHave(index);
		}
	}
	
	/**
	 * 处理dontHave消息
	 */
	private void dontHave(ByteBuffer buffer) throws NetException {
		this.dontHaveExtensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * 创建扩展消息
	 * 
	 * @param type 扩展消息类型
	 * @param bytes 扩展消息数据
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
	 * @param type 扩展消息类型
	 * @param bytes 扩展消息数据
	 */
	public void pushMessage(byte type, byte[] bytes) {
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.EXTENSION, buildMessage(type, bytes));
	}

}
