package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.system.config.PeerConfig;

/**
 * Peer连接
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnectSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnectSession.class);
	
	/**
	 * 评分：每次记分时记录为上次的下载大小，统计时使用当前下载大小减去上次记录值。
	 */
	private AtomicLong mark = new AtomicLong(0);
	
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
	
	/**
	 * 评分
	 */
	public long mark() {
		final long nowSize = peerSession.statistics().uploadSize();
		final long oldSize = mark.getAndSet(nowSize);
		return nowSize - oldSize;
	}

	public void release() {
		LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.port());
		this.peerMessageHandler.choke();
		this.peerMessageHandler.close();
		this.peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
	}
	
}
