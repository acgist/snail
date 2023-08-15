package com.acgist.snail.net.torrent.peer;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * Peer连接信息
 * PeerConnect独立保存连接信息：PeerSession可能存在多个连接
 * Peer：远程连接客户端
 * 客户端：软件本身
 * 
 * @author acgist
 */
public final class PeerConnectSession {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnectSession.class);
    
    /**
     * 评分统计最短时间：{@value}
     */
    private static final long MIN_MARK_INTERVAL = 60L * SystemConfig.ONE_SECOND_MILLIS;

    /**
     * 客户端将Peer阻塞
     * 1：阻塞
     * 0：非阻塞
     * 阻塞后不允许上传数据
     */
    private volatile boolean amChoked;
    /**
     * 客户端对Peer感兴趣
     * 1：感兴趣
     * 0：不感兴趣
     */
    private volatile boolean amInterested;
    /**
     * Peer将客户阻塞
     * 1：阻塞
     * 0：非阻塞
     * 阻塞后不允许下载数据
     */
    private volatile boolean peerChoked;
    /**
     * Peer对客户端感兴趣
     * 1：感兴趣
     * 0：不感兴趣
     */
    private volatile boolean peerInterested;
    /**
     * 上传评分
     */
    private volatile long uploadMark;
    /**
     * 下载评分
     */
    private volatile long downloadMark;
    /**
     * 累计上传大小
     */
    private final AtomicLong uploadSize = new AtomicLong(0);
    /**
     * 累计下载大小
     */
    private final AtomicLong downloadSize = new AtomicLong(0);
    /**
     * 最后一次刷新时间
     */
    private volatile long lastRefreshMarkTime = System.currentTimeMillis();
    
    /**
     * 初始：阻塞、不感兴趣
     * 初始下载评分：不能设置为零，防止连接首次评分就被剔除。
     */
    public PeerConnectSession() {
        this.amChoked       = true;
        this.amInterested   = false;
        this.peerChoked     = true;
        this.peerInterested = false;
        this.uploadMark     = DownloadConfig.getUploadBufferByte();
        this.downloadMark   = DownloadConfig.getDownloadBufferByte();
    }
    
    /**
     * 客户端将Peer阻塞
     */
    public void amChoked() {
        this.amChoked = true;
    }
    
    /**
     * 客户端解除Peer阻塞
     */
    public void amUnchoked() {
        this.amChoked = false;
    }
    
    /**
     * 客户端是否将Peer阻塞
     * 
     * @return 是否阻塞
     */
    public boolean isAmChoked() {
        return this.amChoked;
    }
    
    /**
     * 客户端是否解除Peer阻塞
     * 
     * @return 是否解除阻塞
     */
    public boolean isAmUnchoked() {
        return !this.amChoked;
    }
    
    /**
     * 客户端对Peer感兴趣
     */
    public void amInterested() {
        this.amInterested = true;
    }
    
    /**
     * 客户端对Peer不感兴趣
     */
    public void amNotInterested() {
        this.amInterested = false;
    }
    
    /**
     * 客户端是否对Peer感兴趣
     * 
     * @return 是否感兴趣
     */
    public boolean isAmInterested() {
        return this.amInterested;
    }
    
    /**
     * 客户端是否对Peer不感兴趣
     * 
     * @return 是否不感兴趣
     */
    public boolean isAmNotInterested() {
        return !this.amInterested;
    }
    
    /**
     * Peer将客户端阻塞
     */
    public void peerChoked() {
        this.peerChoked = true;
    }
    
    /**
     * Peer解除客户端阻塞
     */
    public void peerUnchoked() {
        this.peerChoked = false;
    }

    /**
     * Peer是否将客户端阻塞
     * 
     * @return 是否阻塞
     */
    public boolean isPeerChoked() {
        return this.peerChoked;
    }
    
    /**
     * Peer是否解除客户端阻塞
     * 
     * @return 是否解除阻塞
     */
    public boolean isPeerUnchoked() {
        return !this.peerChoked;
    }
    
    /**
     * Peer对客户端感兴趣
     */
    public void peerInterested() {
        this.peerInterested = true;
    }
    
    /**
     * Peer对客户端不感兴趣
     */
    public void peerNotInterested() {
        this.peerInterested = false;
    }
    
    /**
     * Peer是否对客户端感兴趣
     * 
     * @return 是否感兴趣
     */
    public boolean isPeerInterested() {
        return this.peerInterested;
    }
    
    /**
     * Peer是否对客户端不感兴趣
     * 
     * @return 是否不感兴趣
     */
    public boolean isPeerNotInterested() {
        return !this.peerInterested;
    }
    
    /**
     * 判断是否可以上传
     * 可以上传：Peer对客户端感兴趣并且客户端解除Peer阻塞
     * 
     * @return 是否可以上传
     */
    public boolean uploadable() {
        return this.peerInterested && !this.amChoked;
    }
    
    /**
     * 判断是否可以下载
     * 可以下载：客户端对Peer感兴趣并且Peer解除客户阻塞
     * 
     * @return 是否可以下载
     */
    public boolean downloadable() {
        return this.amInterested && !this.peerChoked;
    }
    
    /**
     * 上传计分
     * 
     * @param buffer 上传大小
     */
    public final void upload(int buffer) {
        this.uploadSize.addAndGet(buffer);
    }
    
    /**
     * @return 上传评分
     */
    public final long uploadMark() {
        this.refreshMark();
        return this.uploadMark;
    }
    
    /**
     * 下载计分
     * 
     * @param buffer 下载大小
     */
    public final void download(int buffer) {
        this.downloadSize.addAndGet(buffer);
    }
    
    /**
     * @return 下载评分
     */
    public final long downloadMark() {
        this.refreshMark();
        return this.downloadMark;
    }
    
    /**
     * 刷新评分
     */
    private final void refreshMark() {
        final long nowTime  = System.currentTimeMillis();
        final long interval = nowTime - this.lastRefreshMarkTime;
        if(interval > MIN_MARK_INTERVAL) {
            this.lastRefreshMarkTime = nowTime;
            this.uploadMark          = this.uploadSize.getAndSet(0L);
            this.downloadMark        = this.downloadSize.getAndSet(0L);
            LOGGER.debug("刷新评分：{} - {}", this.uploadMark, this.downloadMark);
        }
    }
    
}
