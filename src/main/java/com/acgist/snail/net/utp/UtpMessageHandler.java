package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;

/**
 * <p>uTorrent transport protocol</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	private PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	public UtpMessageHandler() {
	}

	public UtpMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}

	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress address) {
		// TODO:
		peerLauncherMessageHandler.oneMessage(buffer);
	}
	
}
