package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;

/**
 * <p>Peer连接信息</p>
 * <p>每个Peer连接独立保存连接信息，不能放在PeerSession里面，因为每个Peer可能存在多个连接。</p>
 * 
 * @author acgist
 */
public final class PeerConnectSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnectSession.class);
	
	/**
	 * <p>评分统计最短时间：{@value}</p>
	 */
	private static final long MIN_MARK_INTERVAL = 60L * SystemConfig.ONE_SECOND_MILLIS;

	/**
	 * <p>客户端将Peer阻塞：阻塞-1（true）、非阻塞-0</p>
	 */
	private volatile boolean amChoked;
	/**
	 * <p>客户端对Peer感兴趣：感兴趣-1（true）、不感兴趣-0</p>
	 */
	private volatile boolean amInterested;
	/**
	 * <p>Peer将客户阻塞：阻塞-1（true）、非阻塞-0</p>
	 */
	private volatile boolean peerChoked;
	/**
	 * <p>Peer对客户端感兴趣：感兴趣-1（true）、不感兴趣-0</p>
	 */
	private volatile boolean peerInterested;
	/**
	 * <p>上传评分</p>
	 */
	private volatile long uploadMark;
	/**
	 * <p>下载评分</p>
	 */
	private volatile long downloadMark;
	/**
	 * <p>上次累计上传大小</p>
	 */
	private long lastUploadSize;
	/**
	 * <p>上次累计下载大小</p>
	 */
	private long lastDownloadSize;
	/**
	 * <p>累计上传大小</p>
	 */
	private final AtomicLong uploadSize = new AtomicLong(0);
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize = new AtomicLong(0);
	/**
	 * <p>最后一次刷新时间</p>
	 */
	private volatile long lastRefreshMarkTime = System.currentTimeMillis();
	
	/**
	 * <p>默认：阻塞、不感兴趣</p>
	 */
	public PeerConnectSession() {
		this.amChoked = true;
		this.amInterested = false;
		this.peerChoked = true;
		this.peerInterested = false;
		// 初始积分不能为零：初始连接开始没有数据
		this.uploadMark = DownloadConfig.getUploadBufferByte();
		this.downloadMark = DownloadConfig.getDownloadBufferByte();
	}
	
	/**
	 * <p>客户端将Peer阻塞</p>
	 */
	public void amChoked() {
		this.amChoked = true;
	}
	
	/**
	 * <p>客户端解除Peer阻塞</p>
	 */
	public void amUnchoked() {
		this.amChoked = false;
	}
	
	/**
	 * <p>客户端对Peer感兴趣</p>
	 */
	public void amInterested() {
		this.amInterested = true;
	}
	
	/**
	 * <p>客户端对Peer不感兴趣</p>
	 */
	public void amNotInterested() {
		this.amInterested = false;
	}
	
	/**
	 * <p>Peer将客户端阻塞</p>
	 */
	public void peerChoked() {
		this.peerChoked = true;
	}
	
	/**
	 * <p>Peer解除客户端阻塞</p>
	 */
	public void peerUnchoked() {
		this.peerChoked = false;
	}

	/**
	 * <p>客户端被Peer感兴趣</p>
	 */
	public void peerInterested() {
		this.peerInterested = true;
	}
	
	/**
	 * <p>客户端被Peer不感兴趣</p>
	 */
	public void peerNotInterested() {
		this.peerInterested = false;
	}
	
	/**
	 * <p>客户端是否阻塞Peer</p>
	 * 
	 * @return 是否阻塞
	 */
	public boolean isAmChoked() {
		return this.amChoked;
	}
	
	/**
	 * <p>客户端是否解除Peer阻塞</p>
	 * 
	 * @return 是否解除阻塞
	 */
	public boolean isAmUnchoked() {
		return !this.amChoked;
	}
	
	/**
	 * <p>客户端是否对Peer感兴趣</p>
	 * 
	 * @return 是否感兴趣
	 */
	public boolean isAmInterested() {
		return this.amInterested;
	}
	
	/**
	 * <p>客户端是否对Peer不感兴趣</p>
	 * 
	 * @return 是否不感兴趣
	 */
	public boolean isAmNotInterested() {
		return !this.amInterested;
	}
	
	/**
	 * <p>Peer是否阻塞客户端</p>
	 * 
	 * @return 是否阻塞
	 */
	public boolean isPeerChoked() {
		return this.peerChoked;
	}
	
	/**
	 * <p>Peer是否解除客户端阻塞</p>
	 * 
	 * @return 是否解除阻塞
	 */
	public boolean isPeerUnchoked() {
		return !this.peerChoked;
	}
	
	/**
	 * <p>Peer是否对客户端感兴趣</p>
	 * 
	 * @return 是否感兴趣
	 */
	public boolean isPeerInterested() {
		return this.peerInterested;
	}
	
	/**
	 * <p>Peer是否对客户端不感兴趣</p>
	 * 
	 * @return 是否不感兴趣
	 */
	public boolean isPeerNotInterested() {
		return !this.peerInterested;
	}
	
	/**
	 * <p>判断是否可以上传</p>
	 * <p>可以上传：Peer对客户端感兴趣并且客户端未阻塞Peer</p>
	 * 
	 * @return 是否可以上传
	 */
	public boolean uploadable() {
		return this.peerInterested && !this.amChoked;
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * <p>可以下载：客户端对Peer感兴趣并且Peer未阻塞客户端</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.amInterested && !this.peerChoked;
	}
	
	/**
	 * <p>上传计分</p>
	 * 
	 * @param buffer 上次大小
	 */
	public final void upload(int buffer) {
		this.uploadSize.addAndGet(buffer);
	}
	
	/**
	 * <p>获取上传评分</p>
	 * 
	 * @return 上传评分
	 */
	public final long uploadMark() {
		this.refreshMark();
		return this.uploadMark;
	}
	
	/**
	 * <p>下载计分</p>
	 * 
	 * @param buffer 下载大小
	 */
	public final void download(int buffer) {
		this.downloadSize.addAndGet(buffer);
	}
	
	/**
	 * <p>获取下载评分</p>
	 * 
	 * @return 下载评分
	 */
	public final long downloadMark() {
		this.refreshMark();
		return this.downloadMark;
	}
	
	/**
	 * <p>刷新评分</p>
	 */
	private final void refreshMark() {
		final long nowTime = System.currentTimeMillis();
		final long interval = nowTime - this.lastRefreshMarkTime;
		if(interval > MIN_MARK_INTERVAL) {
			this.lastRefreshMarkTime = nowTime;
			final long uploadSize = this.uploadSize.get();
			this.uploadMark = uploadSize - this.lastUploadSize;
			this.lastUploadSize = uploadSize;
			final long downloadSize = this.downloadSize.get();
			this.downloadMark = downloadSize - this.lastDownloadSize;
			this.lastDownloadSize = downloadSize;
			LOGGER.debug("刷新评分：{}-{}", uploadSize, downloadSize);
		}
	}
	
}
