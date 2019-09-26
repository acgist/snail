package com.acgist.snail.net.torrent.bootstrap;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;

/**
 * <p>Peer连接</p>
 * <p>提供分享上传功能</p>
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnect {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnect.class);
	
	/**
	 * 已被评分：第一次连入还没有被评分。
	 */
	private volatile boolean marked = false;
	/**
	 * 评分：每次记分时记录为上次的下载大小，统计时使用当前下载大小减去上次记录值。
	 */
	private final AtomicLong mark = new AtomicLong(0);
	/**
	 * 状态：连接是否成功
	 */
	private volatile boolean available = false;
	
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
		return this.peerSession;
	}
	
	/**
	 * 发送have消息
	 */
	public void have(int index) {
		this.peerSubMessageHandler.have(index);
	}
	
	/**
	 * 发送pex消息
	 */
	public void pex(byte[] bytes) {
		this.peerSubMessageHandler.pex(bytes);
	}
	
	/**
	 * 已经评分
	 */
	public boolean marked() {
		if(this.marked) {
			return this.marked;
		}
		this.marked = true;
		return false;
	}
	
	/**
	 * 评分，每次记录旧上传大小。
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
		try {
			if(this.available) {
				LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
				this.available = false;
				this.peerSubMessageHandler.choke();
				this.peerSubMessageHandler.close();
			}
		} catch (Exception e) {
			LOGGER.error("PeerConnect关闭异常", e);
		} finally {
			this.peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
			this.peerSession.peerConnect(null);
		}
	}
	
}
