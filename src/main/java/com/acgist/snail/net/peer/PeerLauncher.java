package com.acgist.snail.net.peer;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer下载<br>
 * 权重计算：稀有度>速度
 */
public class PeerLauncher {

	private PeerClient client;

	private final PeerSession peerSession;
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	
	public PeerLauncher(PeerSession peerSession, TorrentSession torrentSession) {
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
	}
	
	public PeerSession peerSession() {
		return peerSession;
	}

}
