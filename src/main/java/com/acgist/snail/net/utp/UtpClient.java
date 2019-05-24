package com.acgist.snail.net.utp;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.net.service.ServiceServer;
import com.acgist.snail.pojo.session.PeerSession;

/**
 * UTP客户端
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpClient extends UdpClient<UtpMessageHandler> {

	private final PeerSession peerSession;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	private UtpClient(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		super("UTP Client", new UtpMessageHandler(peerLauncherMessageHandler, peerSession.peerSocketAddress()), peerSession.peerSocketAddress());
		this.peerSession = peerSession;
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}
	
	public static final UtpClient newInstance(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		return new UtpClient(peerSession, peerLauncherMessageHandler);
	}
	
	@Override
	public boolean open() {
		return open(ServiceServer.getInstance().channel());
	}

	/**
	 * 握手
	 */
	public boolean connect() {
		return this.handler.connect();
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	public PeerLauncherMessageHandler peerLauncherMessageHandler() {
		return this.peerLauncherMessageHandler;
	}
	
}
