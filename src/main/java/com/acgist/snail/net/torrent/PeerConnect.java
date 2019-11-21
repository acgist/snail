package com.acgist.snail.net.torrent;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Peer连接</p>
 * <p>连接：下载（不提供上传）</p>
 * <p>接入：上传、下载（如果接触阻塞可以下载）</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class PeerConnect {

	/**
	 * <p>是否已被评分</p>
	 * <p>第一次获取评分不计分：防止剔除</p>
	 */
	protected volatile boolean marked = false;
	/**
	 * 连接状态
	 */
	protected volatile boolean available = false;
	
	protected final PeerSession peerSession;
	protected final TorrentSession torrentSession;
	protected final PeerSubMessageHandler peerSubMessageHandler;
	
	public PeerConnect(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	public TorrentSession torrentSession() {
		return this.torrentSession;
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
	 * 是否评分
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
	 * @return 上传评分
	 */
	public long uploadMark() {
		return 0L;
	}
	
	/**
	 * @return 下载评分
	 */
	public long downloadMark() {
		return 0L;
	}
	
	/**
	 * 开始下载
	 */
	public abstract void download();
	
	/**
	 * 保存Piece数据
	 * 
	 * @param index Piece索引
	 * @param begin Piece偏移
	 * @param bytes Piece数据
	 */
	public abstract void piece(int index, int begin, byte[] bytes);

	/**
	 * 释放资源：上传
	 */
	public void releaseUpload() {
	}
	
	/**
	 * 释放资源：下载
	 */
	public void releaseDownload() {
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.peerSession.host(), this.peerSession.port(), this.peerSession.dhtPort());
	}
	
}
