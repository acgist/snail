package com.acgist.snail.net.torrent;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer客户端：接入、连接
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class PeerClientHandler {

	/**
	 * <p>是否已被评分</p>
	 * <p>第一次获取评分时忽略评分，防止被剔除。</p>
	 */
	protected volatile boolean marked = false;
	/**
	 * Peer评分
	 */
	protected final AtomicLong mark = new AtomicLong(0);
	/**
	 * 连接状态
	 */
	protected volatile boolean available = false;
	
	protected final PeerSession peerSession;
	protected final PeerSubMessageHandler peerSubMessageHandler;
	
	public PeerClientHandler(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSession = peerSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	/**
	 *   获取PeerSession
	 */
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
	 * 发送holepunch连接消息
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 */
	public void holepunchConnect(String host, int port) {
		this.peerSubMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * 发送uploadOnly消息
	 */
	public void uploadOnly() {
		this.peerSubMessageHandler.uploadOnly();
	}
	
	/**
	 * 是否已经评分
	 */
	public boolean marked() {
		if(this.marked) {
			return this.marked;
		}
		this.marked = true;
		return false;
	}
	
	/**
	 * 是否可用
	 */
	public boolean available() {
		return this.available && this.peerSubMessageHandler.available();
	}
	
	/**
	 * 获取评分
	 * 
	 * @return 评分
	 */
	public abstract long mark();
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.peerSession.host(), this.peerSession.peerPort(), this.peerSession.dhtPort());
	}
	
}
