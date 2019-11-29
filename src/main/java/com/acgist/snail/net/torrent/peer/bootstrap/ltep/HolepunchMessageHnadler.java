package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.PeerConfig.HolepunchErrorCode;
import com.acgist.snail.system.config.PeerConfig.HolepunchType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Holepunch extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0055.html</p>
 * <dl>
 * 	<dt>注意事项</dt>
 * 	<dd>目标方如果已经连接发起方，应该忽略连接消息。</dd>
 * 	<dd>目标方如果不希望连接发起方时，直接忽略连接消息，不能响应错误给中继。</dd>
 * 	<dd>发起方如果没有在扩展协议握手时表示支持holepunch扩展协议，中继应该忽略所有消息。</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HolepunchMessageHnadler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	/**
	 * <p>IPv4</p>
	 */
	private static final byte IPV4 = 0x00;
//	/**
//	 * <p>IPv6</p>
//	 */
//	private static final byte IPV6 = 0x01;
	
	private final TorrentSession torrentSession;
	
	private HolepunchMessageHnadler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_HOLEPUNCH, peerSession, extensionMessageHandler);
		this.torrentSession = torrentSession;
	}
	
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, torrentSession, extensionMessageHandler);
	}

	@Override
	public void doMessage(ByteBuffer buffer) {
		final byte typeId = buffer.get();
		final HolepunchType holepunchType = PeerConfig.HolepunchType.valueOf(typeId);
		if(holepunchType == null) {
			LOGGER.warn("处理holepunch消息错误（类型不支持）：{}", typeId);
			return;
		}
		final byte addrType = buffer.get(); // 地址类型
		int port;
		String host;
		if(addrType == IPV4) {
			host = NetUtils.decodeIntToIp(buffer.getInt());
			port = NetUtils.decodePort(buffer.getShort());
		} else {
			host = null;
			port = 0;
			return; // TODO：IPv6
		}
		LOGGER.debug("处理holepunch消息类型：{}", holepunchType);
		switch (holepunchType) {
		case RENDEZVOUS:
			onRendezvous(host, port);
			break;
		case CONNECT:
			onConnect(host, port);
			break;
		case ERROR:
			final int errorCode = buffer.getInt();
			onError(host, port, errorCode);
			break;
		default:
			LOGGER.info("处理holepunch消息错误（类型未适配）：{}", holepunchType);
			break;
		}
	}
	
	/**
	 * <p>发送消息：rendezvous</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void rendezvous(String host, int port) {
		LOGGER.debug("发送holepunch消息-rendezvous：{}-{}", host, port);
		final ByteBuffer message = buildMessage(HolepunchType.RENDEZVOUS, host, port);
		this.pushMessage(message);
	}
	
	/**
	 * <p>处理消息：rendezvous</p>
	 * <p>如果已经连接到目标方，同时发送连接消息给发起方和目标方，其他情况返回相应错误。</p>
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 */
	private void onRendezvous(String host, int port) {
		LOGGER.debug("处理holepunch消息-rendezvous：{}-{}", host, port);
		final String extIp = SystemConfig.getExternalIpAddress();
		if(StringUtils.equals(host, extIp)) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标属于中继");
			this.error(host, port, HolepunchErrorCode.CODE_04);
			return;
		}
		// 目标Peer
		final var peerSession = PeerManager.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host);
		// 目标不存在
		if(peerSession == null) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标不存在");
			this.error(host, port, HolepunchErrorCode.CODE_01);
			return;
		}
		// 目标未连接
		if(!peerSession.connected()) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标未连接");
			this.error(host, port, HolepunchErrorCode.CODE_02);
			return;
		}
		// 目标不支持协议
		if(!peerSession.supportExtensionType(ExtensionType.UT_HOLEPUNCH)) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标不支持协议");
			this.error(host, port, HolepunchErrorCode.CODE_03);
			return;
		}
		// 发送发送方连接消息
		this.connect(host, port);
		// 发送目标方连接消息
		final var peerUploader = peerSession.peerUploader();
		if(peerUploader != null) {
			peerUploader.holepunchConnect(this.peerSession.host(), this.peerSession.port());
			return;
		}
		final var peerDownloader = peerSession.peerDownloader();
		if(peerDownloader != null) {
			peerDownloader.holepunchConnect(this.peerSession.host(), this.peerSession.port());
			return;
		}
	}
	
	/**
	 * <p>发送消息：connect</p>
	 */
	public void connect(String host, int port) {
		LOGGER.debug("发送holepunch消息-connect：{}-{}", host, port);
		this.pushMessage(buildMessage(HolepunchType.CONNECT, host, port));
	}
	
	/**
	 * <p>处理消息：connect</p>
	 */
	public void onConnect(String host, int port) {
		LOGGER.debug("处理holepunch消息-connect：{}-{}", host, port);
		var peerSession = PeerManager.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host);
		if(peerSession == null) { // 没有时创建
			peerSession = PeerManager.getInstance().newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.SOURCE_HOLEPUNCH);
		}
		if(peerSession.connected()) {
			LOGGER.debug("处理holepunch消息-connect：目标已连接");
			return;
		}
		final var peerSubMessageHandler = PeerSubMessageHandler.newInstance(peerSession, this.torrentSession);
		final var client = UtpClient.newInstance(peerSession, peerSubMessageHandler);
		if(client.connect()) {
			LOGGER.debug("处理holepunch消息-connect：连接成功");
			peerSession.flags(PeerConfig.PEX_UTP);
			client.close();
		} else {
			LOGGER.debug("处理holepunch消息-connect：连接失败");
		}
	}

	/**
	 * <p>发送消息：error</p>
	 */
	private void error(String host, int port, HolepunchErrorCode errorCode) {
		LOGGER.debug("发送holepunch消息-error：{}-{}-{}", host, port, errorCode);
		this.pushMessage(buildMessage(HolepunchType.ERROR, host, port, errorCode));
	}
	
	/**
	 * <p>处理消息：error</p>
	 */
	private void onError(String host, int port, int errorCode) {
		LOGGER.warn("处理holepunch消息-error：{}-{}-{}", host, port, errorCode);
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @see #buildMessage(HolepunchType, String, int, HolepunchErrorCode)
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port) {
		return this.buildMessage(type, host, port, null);
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host 地址
	 * @param port 端口
	 * @param errorCode 错误编码：非错误消息=null
	 * 
	 * TODO：IPv6
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port, HolepunchErrorCode errorCode) {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.put(type.id()); // 消息类型
		buffer.put(IPV4); // 地址类型：0x00=IPv4；0x01=IPv6；
		buffer.putInt(NetUtils.encodeIpToInt(host)); // IP地址
		buffer.putShort(NetUtils.encodePort(port)); // 端口号
		if(type == HolepunchType.ERROR) { // 非错误消息不发送错误编码
			buffer.putInt(errorCode.code()); // 错误编码
		}
		return buffer;
	}
	
}
