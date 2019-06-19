package com.acgist.snail.net.bt.peer.bootstrap.dht;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.net.bt.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtExtensionMessageHandler.class);
	
	private final DhtLauncher dhtLauncher;
	private final PeerSession peerSession;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;

	public static final DhtExtensionMessageHandler newInstance(PeerSession peerSession, DhtLauncher dhtLauncher, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		return new DhtExtensionMessageHandler(peerSession, dhtLauncher, peerLauncherMessageHandler);
	}
	
	private DhtExtensionMessageHandler(PeerSession peerSession, DhtLauncher dhtLauncher, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerSession = peerSession;
		this.dhtLauncher = dhtLauncher;
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}
	
	public void onMessage(ByteBuffer buffer) {
		port(buffer);
	}

	public void port() {
		final byte[] bytes = ByteBuffer.allocate(2).putShort(SystemConfig.getBtPortExtShort()).array();
		this.peerLauncherMessageHandler.pushMessage(PeerConfig.Type.dht, bytes);
	}
	
	/**
	 * <p>处理DHT消息</p>
	 * <p>设置DHT端口，加入DHT列表。</p>
	 */
	private void port(ByteBuffer buffer) {
		LOGGER.debug("收到DHT消息");
		final int port = NetUtils.decodePort(buffer.getShort());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("DHT扩展添加DHT节点：{}-{}", this.peerSession.host(), port);
		}
		this.peerSession.dhtPort(port);
		if(this.dhtLauncher != null) {
			this.dhtLauncher.put(this.peerSession.host(), port);
		}
		SystemThreadContext.submit(() -> {
			NodeManager.getInstance().newNodeSession(this.peerSession.host(), port);
		});
	}

}
