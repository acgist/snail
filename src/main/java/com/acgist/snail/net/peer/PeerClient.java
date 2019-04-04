package com.acgist.snail.net.peer;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 * https://blog.csdn.net/li6322511/article/details/79002753
 * https://blog.csdn.net/p312011150/article/details/81478237
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {

	private PeerSession peerSession;
	private TaskSession taskSession;
	private TorrentSession torrentSession;
	
	public PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer", new PeerMessageHandler(torrentSession));
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
	}

	@Override
	public boolean connect() {
		return false;
	}

}
