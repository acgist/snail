package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionTypeGetter;
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
 * 	<dd>目标方如果不希望连接发起方时，直接忽略连接消息，不能响应错误给中继。</dd>
 * 	<dd>发起方如果没有在扩展协议握手时表示支持holepunch扩展协议，中继应该忽略所有消息。</dd>
 * 	<dd>目标方如果已经连接发起方，应该忽略连接消息。</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HolepunchMessageHnadler implements IExtensionMessageHandler, IExtensionTypeGetter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	private static final byte IPV4 = 0x00;
//	private static final byte IPV6 = 0x01;
	
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final ExtensionMessageHandler extensionMessageHandler;
	
	private HolepunchMessageHnadler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, torrentSession, extensionMessageHandler);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		final Byte type = extensionType();
		if(type == null) {
			LOGGER.debug("holepunch消息错误：Peer不支持");
			return;
		}
		final byte typeId = buffer.get();
		final HolepunchType holepunchType = PeerConfig.HolepunchType.valueOf(typeId);
		if(holepunchType == null) {
			LOGGER.warn("holepunch消息错误（类型不支持）：{}", typeId);
			return;
		}
		final byte addrType = buffer.get();
		int port, errorCode;
		String host;
		if(addrType == IPV4) {
			host = NetUtils.decodeIntToIp(buffer.getInt());
			port = NetUtils.decodePort(buffer.getShort());
		} else {
			// TODO：IPv6
			host = null;
			port = 0;
		}
		errorCode = buffer.getInt();
		LOGGER.debug("holepunch消息类型：{}", holepunchType);
		switch (holepunchType) {
		case RENDEZVOUS:
			onRendezvous(host, port);
			break;
		case CONNECT:
			onConnect(host, port);
			break;
		case ERROR:
			onError(host, port, errorCode);
			break;
		default:
			LOGGER.info("holepunch消息错误（类型未适配）：{}", holepunchType);
			break;
		}
	}
	
	@Override
	public Byte extensionType() {
		return this.peerSession.extensionTypeValue(ExtensionType.UT_HOLEPUNCH);
	}
	
	/**
	 * 发送连接消息
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void holepunch(String host, int port) {
		this.rendezvous(host, port);
	}
	
	/**
	 * 发送消息：rendezvous
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void rendezvous(String host, int port) {
		LOGGER.debug("发送holepunch消息-rendezvous：{}-{}", host, port);
		final ByteBuffer message = buildMessage(HolepunchType.RENDEZVOUS, host, port, HolepunchErrorCode.CODE_00);
		pushMessage(message);
	}
	
	/**
	 * <p>处理消息：rendezvous</p>
	 * <p>如果已经连接到目标方，返回连接消息，其他情况返回相应错误。</p>
	 */
	private void onRendezvous(String host, int port) {
		LOGGER.debug("处理holepunch消息-rendezvous：{}-{}", host, port);
		final String local = SystemConfig.getExternalIpAddress();
		if(StringUtils.equals(host, local)) {
			LOGGER.debug("holepunch消息-rendezvous处理失败：目标属于中继");
			this.error(host, port, HolepunchErrorCode.CODE_04);
			return;
		}
		final var peerSession = PeerManager.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host);
		if(peerSession == null) {
			// TODO：是否加入Peer列表
			LOGGER.debug("holepunch消息-rendezvous处理失败：目标不存在");
			this.error(host, port, HolepunchErrorCode.CODE_01);
			return;
		}
		if(!peerSession.connected()) {
			LOGGER.debug("holepunch消息-rendezvous处理失败：目标未连接");
			this.error(host, port, HolepunchErrorCode.CODE_02);
			return;
		}
		if(!peerSession.supportExtensionType(ExtensionType.UT_HOLEPUNCH)) {
			LOGGER.debug("holepunch消息-rendezvous处理失败：目标不支持协议");
			this.error(host, port, HolepunchErrorCode.CODE_03);
			return;
		}
		this.connect(host, port);
	}
	
	/**
	 * 发送消息：connect
	 */
	private void connect(String host, int port) {
		LOGGER.debug("发送holepunch消息-connect：{}-{}", host, port);
		this.pushMessage(buildMessage(HolepunchType.CONNECT, host, port, HolepunchErrorCode.CODE_00));
	}
	
	/**
	 * 处理消息：connect
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
			peerSession.flags(PeerConfig.PEX_UTP);
			client.close();
		}
	}

	/**
	 * 发送消息：error
	 */
	private void error(String host, int port, HolepunchErrorCode errorCode) {
		LOGGER.debug("发送holepunch消息-error：{}-{}-{}", host, port, errorCode);
		this.pushMessage(buildMessage(HolepunchType.ERROR, host, port, errorCode));
	}
	
	/**
	 * 处理消息：error
	 */
	private void onError(String host, int port, int errorCode) {
		LOGGER.warn("处理holepunch消息-error：{}-{}-{}", host, port, errorCode);
	}
	
	/**
	 * 创建消息
	 * 
	 * TODO：IPv6
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port, HolepunchErrorCode errorCode) {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.put(type.id()); // 消息类型
		buffer.put(IPV4); // 地址类型：0x00=IPv4；0x01=IPv6；
		buffer.putInt(NetUtils.encodeIpToInt(host)); // IP地址
		buffer.putShort(NetUtils.encodePort(port)); // 端口号
		buffer.putInt(errorCode.code()); // 错误编码
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		final Byte type = extensionType();
		if (type == null) {
			LOGGER.warn("不支持holepunch扩展协议");
			return;
		}
		this.extensionMessageHandler.pushMessage(type, buffer.array());
	}

}
