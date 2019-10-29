package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;

/**
 * <p>Peer客户端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {

	private final PeerSession peerSession;
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	private PeerClient(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		super("Peer Client", CONNECT_TIMEOUT, new PeerMessageHandler(peerSubMessageHandler));
		this.peerSession = peerSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	public static final PeerClient newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerClient(peerSession, peerSubMessageHandler);
	}
	
	@Override
	public boolean connect() {
		return connect(this.peerSession.host(), this.peerSession.port());
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	public PeerSubMessageHandler peerSubMessageHandler() {
		return this.peerSubMessageHandler;
	}
	
}
