package com.acgist.snail.net.bt.bootstrap;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.peer.bootstrap.PeerLauncherMessageHandler;
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
	
	private volatile boolean available = false; // 状态：连接是否成功
	
	private final PeerSession peerSession;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	private PeerConnect(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.available = true;
		this.peerSession = peerSession;
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}
	
	public static final PeerConnect newInstance(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		return new PeerConnect(peerSession, peerLauncherMessageHandler);
	}

	public PeerSession peerSession() {
		return peerSession;
	}
	
	/**
	 * 评分
	 */
	public long mark() {
		final long nowSize = this.peerSession.statistics().uploadSize();
		final long oldSize = this.mark.getAndSet(nowSize);
		return nowSize - oldSize;
	}

	/**
	 * 是否可用
	 */
	public boolean available() {
		return this.available && this.peerLauncherMessageHandler.available();
	}
	
	/**
	 * 释放资源：阻塞、关闭Socket，设置非上传状态。
	 */
	public void release() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
		}
		this.available = false;
		this.peerLauncherMessageHandler.choke();
		this.peerLauncherMessageHandler.close();
		this.peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
	}
	
}
