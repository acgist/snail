package com.acgist.snail.net.bt.bootstrap;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.peer.bootstrap.PeerSubMessageHandler;
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
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	private PeerConnect(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.available = true;
		this.peerSession = peerSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	public static final PeerConnect newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerConnect(peerSession, peerSubMessageHandler);
	}

	public PeerSession peerSession() {
		return peerSession;
	}
	
	/**
	 * 发送have消息
	 */
	public void have(int index) {
		this.peerSubMessageHandler.have(index);
	}
	
	/**
	 * 发送Pex消息
	 */
	public void exchange(byte[] bytes) {
		this.peerSubMessageHandler.exchange(bytes);
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
		return this.available && this.peerSubMessageHandler.available();
	}
	
	/**
	 * 释放资源：阻塞、关闭Socket，设置非上传状态。
	 */
	public void release() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
		}
		this.available = false;
		this.peerSubMessageHandler.choke();
		this.peerSubMessageHandler.close();
		this.peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
		this.peerSession.peerConnect(null);
	}
	
}
