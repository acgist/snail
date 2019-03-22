package com.acgist.snail.net.peer;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer下载<br>
 * 权重计算：稀有度>速度
 * 没下载完成一个Piece才会写入文件
 */
public class PeerLauncher {

	private PeerClient client;

	private final PeerSession peerSession;
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	
	private boolean available = true; // 可用
	
	public PeerLauncher(PeerSession peerSession, TorrentSession torrentSession) {
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
	}
	
	public PeerSession peerSession() {
		return peerSession;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		this.available = false;
	}
	
}
