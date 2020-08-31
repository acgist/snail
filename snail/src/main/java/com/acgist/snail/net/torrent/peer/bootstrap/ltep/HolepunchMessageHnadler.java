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
 * 	<dd>发起方如果没有在扩展协议握手时表示支持holepunch扩展协议，中继应该忽略所有消息。（没有实现）</dd>
 * </dl>
 * <p>Pex交换的Peer如果不能直接连接，Pex源Peer作为中继通过holepunch协议实现连接。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class HolepunchMessageHnadler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	/**
	 * <p>IPv4：{@value}</p>
	 */
	private static final byte IPV4 = 0x00;
//	/**
//	 * <p>IPv6：{@value}</p>
//	 */
//	private static final byte IPV6 = 0x01;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	private HolepunchMessageHnadler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_HOLEPUNCH, peerSession, extensionMessageHandler);
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>创建holepunch代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param extensionMessageHandler 扩展协议代理
	 * 
	 * @return holepunch代理
	 */
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, torrentSession, extensionMessageHandler);
	}

	@Override
	public void doMessage(ByteBuffer buffer) {
		final byte typeId = buffer.get();
		final HolepunchType holepunchType = PeerConfig.HolepunchType.of(typeId);
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
			this.onRendezvous(host, port);
			break;
		case CONNECT:
			this.onConnect(host, port);
			break;
		case ERROR:
			final int errorCode = buffer.getInt();
			this.onError(host, port, errorCode);
			break;
		default:
			LOGGER.info("处理holepunch消息错误（类型未适配）：{}", holepunchType);
			break;
		}
	}
	
	/**
	 * <p>发送消息：rendezvous</p>
	 * 
	 * @param peerSession peerSession
	 */
	public void rendezvous(PeerSession peerSession) {
		final String host = peerSession.host();
		final int port = peerSession.port();
		LOGGER.debug("发送holepunch消息-rendezvous：{}-{}", host, port);
		final ByteBuffer message = this.buildMessage(HolepunchType.RENDEZVOUS, host, port);
		this.pushMessage(message);
		peerSession.holepunchLock(); // 加锁
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
		final var peerSession = PeerManager.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
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
		this.pushMessage(buildMessage(HolepunchType.CONNECT, host, port));
	}
	
	/**
	 * <p>处理消息：connect</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	private void onConnect(String host, int port) {
		LOGGER.debug("处理holepunch消息-connect：{}-{}", host, port);
		var peerSession = PeerManager.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
		if(peerSession == null) { // 没有时创建
			peerSession = PeerManager.getInstance().newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.SOURCE_HOLEPUNCH
			);
		}
		if(peerSession.holepunchWait()) {
			LOGGER.debug("处理holepunch消息-connect：释放holepunch等待锁");
			peerSession.unlockHolepunch();
		} else {
			if(peerSession.connected()) {
				// 已经连接忽略消息
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
		this.pushMessage(buildMessage(HolepunchType.ERROR, host, port, errorCode));
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
	 * <p>创建消息</p>
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
	 * <p>创建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码：非错误消息等于{@code null}
	 * 
	 * @return 消息
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
