package com.acgist.snail.net.torrent.peer.extension;

import java.nio.ByteBuffer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.ExtensionType;
import com.acgist.snail.config.PeerConfig.HolepunchErrorCode;
import com.acgist.snail.config.PeerConfig.HolepunchType;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.peer.ExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.ExtensionTypeMessageHandler;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpClient;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Holepunch extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0055.html</p>
 * <p>Pex交换的Peer如果不能直接连接，Pex源Peer作为中继通过holepunch协议实现连接。</p>
 * 
 * @author acgist
 */
public final class HolepunchMessageHnadler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	/**
	 * <p>IPv4：{@value}</p>
	 */
	private static final byte IPV4 = 0x00;
	/**
	 * <p>IPv6：{@value}</p>
	 */
	private static final byte IPV6 = 0x01;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param extensionMessageHandler 扩展协议代理
	 */
	private HolepunchMessageHnadler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_HOLEPUNCH, peerSession, extensionMessageHandler);
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>新建holepunch扩展协议代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param extensionMessageHandler 扩展协议代理
	 * 
	 * @return holepunch扩展协议代理
	 */
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, torrentSession, extensionMessageHandler);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>发起方在扩展协议握手时没有表示支持holepunch扩展协议：中继应该忽略所有消息</p>
	 * 
	 * @see HolepunchMessageHnadler#onMessage(ByteBuffer)
	 */
	@Override
	public void doMessage(ByteBuffer buffer) {
		final byte typeId = buffer.get();
		final HolepunchType holepunchType = PeerConfig.HolepunchType.of(typeId);
		if(holepunchType == null) {
			LOGGER.warn("处理holepunch消息错误（未知类型）：{}", typeId);
			return;
		}
		final int port;
		final String host;
		final byte addrType = buffer.get();
		if(addrType == IPV4) {
			host = NetUtils.intToIP(buffer.getInt());
			port = NetUtils.portToInt(buffer.getShort());
		} else if(addrType == IPV6) {
			final byte[] bytes = NetUtils.bufferToIPv6(buffer);
			host = NetUtils.bytesToIP(bytes);
			port = NetUtils.portToInt(buffer.getShort());
		} else {
			LOGGER.error("处理holepunch消息错误（不支持的IP协议类型）：{}", addrType);
			return;
		}
		LOGGER.debug("处理holepunch消息：{}", holepunchType);
		switch (holepunchType) {
			case RENDEZVOUS -> this.onRendezvous(host, port);
			case CONNECT -> this.onConnect(host, port);
			case ERROR -> this.onError(host, port, buffer.getInt());
			default -> LOGGER.warn("处理holepunch消息错误（类型未适配）：{}", holepunchType);
		}
	}
	
	/**
	 * <p>发送消息：rendezvous</p>
	 * 
	 * @param peerSession Peer信息
	 */
	public void rendezvous(PeerSession peerSession) {
		final String host = peerSession.host();
		final int port = peerSession.port();
		LOGGER.debug("发送holepunch消息-rendezvous：{}-{}", host, port);
		this.pushMessage(this.buildMessage(HolepunchType.RENDEZVOUS, host, port));
		peerSession.lockHolepunch();
	}
	
	/**
	 * <p>处理消息：rendezvous</p>
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 */
	private void onRendezvous(String host, int port) {
		LOGGER.debug("处理holepunch消息-rendezvous：{}-{}", host, port);
		if(StringUtils.equals(host, SystemConfig.getExternalIPAddress())) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标属于中继");
			this.error(host, port, HolepunchErrorCode.CODE_04);
			return;
		}
		// 目标Peer
		final var peerSession = PeerContext.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
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
		// 向发起方发送目标方连接消息
		this.connect(host, port);
		// 向目标方发送发起方连接消息
		final var peerConnect = peerSession.peerConnect();
		if(peerConnect != null) {
			peerConnect.holepunchConnect(this.peerSession.host(), this.peerSession.port());
		} else {
			LOGGER.warn("处理holepunch消息-rendezvous失败：目标失效");
		}
	}
	
	/**
	 * <p>发送消息：connect</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	public void connect(String host, int port) {
		LOGGER.debug("发送holepunch消息-connect：{}-{}", host, port);
		this.pushMessage(this.buildMessage(HolepunchType.CONNECT, host, port));
	}
	
	/**
	 * <p>处理消息：connect</p>
	 * <p>如果目标方不希望连接发起方时，直接忽略连接消息，不能响应错误给中继。</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	private void onConnect(String host, int port) {
		LOGGER.debug("处理holepunch消息-connect：{}-{}", host, port);
		var peerSession = PeerContext.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
		if(peerSession == null) {
			peerSession = PeerContext.getInstance().newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.Source.HOLEPUNCH
			);
		}
		if(peerSession.holepunchWait()) {
			// 发起方：等待响应
			LOGGER.debug("处理holepunch消息-connect：释放holepunch等待锁");
			peerSession.unlockHolepunch();
		} else {
			// 目标方：主动连接
			if(peerSession.connected()) {
				// 已经连接忽略消息：不用响应信息给中继
				LOGGER.debug("处理holepunch消息-connect：目标已连接");
				return;
			}
			// 发起连接
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
	}

	/**
	 * <p>发送消息：error</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码
	 */
	private void error(String host, int port, HolepunchErrorCode errorCode) {
		LOGGER.debug("发送holepunch消息-error：{}-{}-{}", host, port, errorCode);
		this.pushMessage(this.buildMessage(HolepunchType.ERROR, host, port, errorCode));
	}
	
	/**
	 * <p>处理消息：error</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码
	 */
	private void onError(String host, int port, int errorCode) {
		LOGGER.warn("处理holepunch消息-error：{}-{}-{}", host, port, errorCode);
	}
	
	/**
	 * <p>新建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @return 消息
	 * 
	 * @see #buildMessage(HolepunchType, String, int, HolepunchErrorCode)
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port) {
		return this.buildMessage(type, host, port, null);
	}
	
	/**
	 * <p>新建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码
	 * 
	 * @return 消息
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port, HolepunchErrorCode errorCode) {
		final boolean ipv4 = NetUtils.ipv4(host);
		final ByteBuffer buffer;
		if(ipv4) {
			buffer = ByteBuffer.allocate(12);
			buffer.put(type.getId());
			buffer.put(IPV4);
			buffer.putInt(NetUtils.ipToInt(host));
		} else {
			buffer = ByteBuffer.allocate(24);
			buffer.put(type.getId());
			buffer.put(IPV6);
			buffer.put(NetUtils.ipToBytes(host));
		}
		buffer.putShort(NetUtils.portToShort(port));
		if(type == HolepunchType.ERROR && errorCode != null) {
			buffer.putInt(errorCode.getCode());
		} else {
			// 非错误消息填充零
			buffer.putInt(0);
		}
		return buffer;
	}
	
}
