package com.acgist.snail.pojo.session;

import com.acgist.snail.net.peer.PeerMessageHandler;

/**
 * Peer连接
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnectSession {

	private final PeerSession peerSession;
	private final PeerMessageHandler peerMessageHandler;
	
	private PeerConnectSession(PeerSession peerSession, PeerMessageHandler peerMessageHandler) {
		this.peerSession = peerSession;
		this.peerMessageHandler = peerMessageHandler;
	}
	
	public static final PeerConnectSession newInstance(PeerSession peerSession, PeerMessageHandler peerMessageHandler) {
		return new PeerConnectSession(peerSession, peerMessageHandler);
	}

	public PeerSession getPeerSession() {
		return peerSession;
	}

	public PeerMessageHandler getPeerMessageHandler() {
		return peerMessageHandler;
	}
	
}
