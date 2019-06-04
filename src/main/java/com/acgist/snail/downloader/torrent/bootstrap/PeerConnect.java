package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;

/**
 * Peer连接
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnect {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnect.class);
	
	/**
	 * 评分：每次记分时记录为上次的下载大小，统计时使用当前下载大小减去上次记录值。
	 */
	private AtomicLong mark = new AtomicLong(0);
	
	private final PeerSession peerSession;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	private PeerConnect(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerSession = peerSession;
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}
	
	public static final PeerConnect newInstance(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		return new PeerConnect(peerSession, peerLauncherMessageHandler);
	}

	public PeerSession getPeerSession() {
		return peerSession;
	}

	public PeerLauncherMessageHandler getPeerLauncherMessageHandler() {
		return peerLauncherMessageHandler;
	}
	
	/**
	 * 评分
	 */
	public long mark() {
		final long nowSize = peerSession.statistics().uploadSize();
		final long oldSize = mark.getAndSet(nowSize);
		return nowSize - oldSize;
	}

	/**
	 * 释放资源：阻塞、关闭Socket，设置非上传状态。
	 */
	public void release() {
		LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
		this.peerLauncherMessageHandler.choke();
		this.peerLauncherMessageHandler.close();
		this.peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
	}
	
}
