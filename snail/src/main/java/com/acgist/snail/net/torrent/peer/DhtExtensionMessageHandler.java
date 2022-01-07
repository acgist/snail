package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>DHT Extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * 
 * @author acgist
 */
public final class DhtExtensionMessageHandler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtExtensionMessageHandler.class);
	
	/**
	 * <p>Peer信息</p>
	 */
	private final PeerSession peerSession;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param peerSubMessageHandler Peer消息代理
	 */
	private DhtExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	/**
	 * <p>新建DHT扩展协议代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return DHT扩展协议代理
	 */
	public static final DhtExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new DhtExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) {
		this.port(buffer);
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
	 * <p>处理DHT消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void port(ByteBuffer buffer) {
		final int port = NetUtils.portToInt(buffer.getShort());
		final String host = this.peerSession.host();
		LOGGER.debug("处理DHT消息：{}-{}", host, port);
		this.peerSession.dhtPort(port);
		this.torrentSession.newNode(host, port);
	}

}
