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

/**
 * <p>DHT Extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtExtensionMessageHandler implements IExtensionMessageHandler {
	
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

	public void port() {
		final byte[] bytes = ByteBuffer.allocate(2).putShort(SystemConfig.getTorrentPortExtShort()).array();
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.dht, bytes);
	}
	
	/**
	 * <p>处理DHT消息</p>
	 * <p>设置DHT端口，加入DHT列表。</p>
	 */
	private void port(ByteBuffer buffer) {
		LOGGER.debug("收到DHT消息");
		final int port = NetUtils.decodePort(buffer.getShort());
		LOGGER.debug("DHT扩展添加DHT节点：{}-{}", this.peerSession.host(), port);
		this.peerSession.dhtPort(port);
		final var dhtLauncher = this.torrentSession.dhtLauncher();
		if(dhtLauncher != null) {
			dhtLauncher.put(this.peerSession.host(), port);
		}
	}

}
