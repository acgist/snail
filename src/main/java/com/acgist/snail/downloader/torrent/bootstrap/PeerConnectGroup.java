package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerConnectSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Peer连接组：上传</p>
 * <p>
 * 对连接请求下载的PeerClient管理优化：<br>
 * <ul>
 * 	<li>清除长时间没有请求的Peer。</li>
 * 	<li>不能超过最大分享连接数。</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnectGroup {

	private final BlockingQueue<PeerConnectSession> peerConnectSessions;
	
	private PeerConnectGroup() {
		peerConnectSessions = new LinkedBlockingQueue<>();
	}
	
	public static final PeerConnectGroup newInstance(TorrentSession torrentSession) {
		return new PeerConnectGroup();
	}
	
	/**
	 * <p>是否创建成功</p>
	 * <p>如果超过了连接的最大数量将返回失败</p>
	 */
	public boolean newConnect(PeerSession peerSession, PeerMessageHandler peerMessageHandler) {
		synchronized (this.peerConnectSessions) {
			if(this.peerConnectSessions.size() >= SystemConfig.getPeerSize()) {
				return false;
			}
			final PeerConnectSession session = PeerConnectSession.newInstance(peerSession, peerMessageHandler);
			this.peerConnectSessions.add(session);
		}
		return true;
	}
	
	/**
	 * 优化
	 */
	public void optimize() {
		
	}
	
}
