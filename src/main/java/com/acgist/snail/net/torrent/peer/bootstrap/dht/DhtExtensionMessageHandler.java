package com.acgist.snail.net.torrent.peer.bootstrap.dht;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>DHT Extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DhtExtensionMessageHandler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtExtensionMessageHandler.class);
	
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	private final PeerSubMessageHandler peerSubMessageHandler;

	public static final DhtExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new DhtExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	private DhtExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) {
		port(buffer);
	}

	/**
	 * <p>发送DHT消息</p>
	 */
	public void port() {
		LOGGER.debug("发送DHT消息");
		final byte[] bytes = NumberUtils.shortToBytes(SystemConfig.getTorrentPortExtShort());
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.DHT, bytes);
	}
	
	/**
	 * <p>处理DHT消息：设置DHT端口、添加DHT节点</p>
	 */
	private void port(ByteBuffer buffer) {
		final int port = NetUtils.decodePort(buffer.getShort());
		LOGGER.debug("处理DHT消息：{}", port);
		this.peerSession.dhtPort(port);
		this.torrentSession.newDhtNode(this.peerSession.host(), port);
	}

}
