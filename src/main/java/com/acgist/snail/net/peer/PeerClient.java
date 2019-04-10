package com.acgist.snail.net.peer;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {

	private TorrentPiece downloadPiece; // 下载的Piece信息
	
	private PeerSession peerSession;
	private TaskSession taskSession;
	private TorrentSession torrentSession;
	
	public PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer", 10, new PeerMessageHandler(peerSession, torrentSession));
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
	}

	@Override
	public boolean connect() {
		boolean ok = connect(peerSession.host(), peerSession.port());
		if(ok) {
			handler.handshake();
		}
		return ok;
	}
	
	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	public void release() {
	}

	public void piece(int index, int begin, byte[] bytes) {
	}

	/**
	 * 开始
	 */
	public void request() {
	}

}
