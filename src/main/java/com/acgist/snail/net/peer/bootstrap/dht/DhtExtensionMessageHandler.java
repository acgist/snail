package com.acgist.snail.net.peer.bootstrap.dht;

import java.nio.ByteBuffer;

import com.acgist.snail.downloader.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerMessageConfig;
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
		final byte[] bytes = ByteBuffer.allocate(2).putShort(SystemConfig.getServicePortExtShort()).array();
		this.peerLauncherMessageHandler.pushMessage(PeerMessageConfig.Type.dht, bytes);
	}
	
	private void port(ByteBuffer buffer) {
		final int port = NetUtils.decodePort(buffer.getShort());
		this.peerSession.dhtPort(port);
		if(dhtLauncher != null) {
			dhtLauncher.put(peerSession.host(), port);
		}
		SystemThreadContext.submit(() -> {
			NodeManager.getInstance().newNodeSession(peerSession.host(), port);
		});
	}

}
