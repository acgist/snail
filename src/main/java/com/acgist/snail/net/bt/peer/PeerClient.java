package com.acgist.snail.net.bt.peer;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.bt.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;

/**
 * <p>Peer客户端</p>
 * <p>基本协议：TCP</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {

	private final PeerSession peerSession;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	private PeerClient(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		super("Peer Client", 2, new PeerMessageHandler(peerLauncherMessageHandler));
		this.peerSession = peerSession;
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}

	public static final PeerClient newInstance(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		return new PeerClient(peerSession, peerLauncherMessageHandler);
	}
	
	@Override
	public boolean connect() {
		return connect(this.peerSession.host(), this.peerSession.peerPort());
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	public PeerLauncherMessageHandler peerLauncherMessageHandler() {
		return this.peerLauncherMessageHandler;
	}
	
}
