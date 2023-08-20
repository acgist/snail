package com.acgist.snail.net.torrent.peer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;

/**
 * PeerUploader组
 * 主要功能：接入PeerUploader、清除劣质PeerUploader
 * 
 * @author acgist
 */
public final class PeerUploaderGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerUploaderGroup.class);
    
    /**
     * BT任务信息
     */
    private final TorrentSession torrentSession;
    /**
     * PeerUploader队列
     */
    private final BlockingQueue<PeerUploader> peerUploaders;
    
    /**
     * @param torrentSession BT任务信息
     */
    private PeerUploaderGroup(TorrentSession torrentSession) {
        this.torrentSession = torrentSession;
        this.peerUploaders = new LinkedBlockingQueue<>();
    }
    
    /**
     * 新建PeerUploader组
     * 
     * @param torrentSession BT任务信息
     * 
     * @return {@link PeerUploaderGroup}
     */
    public static final PeerUploaderGroup newInstance(TorrentSession torrentSession) {
        return new PeerUploaderGroup(torrentSession);
    }
    
    /**
     * 开始下载
     * 如果Peer接入支持下载：发送下载请求
     */
    public void download() {
        synchronized (this.peerUploaders) {
            this.peerUploaders.forEach(PeerUploader::download);
        }
    }
    
    /**
     * 新建Peer接入连接
     * 
     * @param peerSession Peer信息
     * @param peerSubMessageHandler Peer消息代理
     * 
     * @return {@link PeerUploader}
     */
    public PeerUploader newPeerUploader(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
        synchronized (this.peerUploaders) {
            if(this.connectable(peerSession)) {
                LOGGER.debug("Peer接入成功：{}", peerSession);
            } else {
                LOGGER.debug("Peer接入失败：{}", peerSession);
                return null;
            }
            final PeerUploader peerUploader = PeerUploader.newInstance(peerSession, this.torrentSession, peerSubMessageHandler);
            peerSession.status(PeerConfig.STATUS_UPLOAD);
            this.offer(peerUploader);
            return peerUploader;
        }
    }
    
    /**
     * 判断是否允许连接
     * 
     * @param peerSession Peer信息
     * 
     * @return 是否允许连接
     */
    private boolean connectable(PeerSession peerSession) {
        if(peerSession.downloading()) {
            // 正在下载：允许连接
            return true;
        } else {
            return this.peerUploaders.size() < SystemConfig.getPeerSize();
        }
    }
    
    /**
     * 优化PeerUploader
     */
    public void optimize() {
        LOGGER.debug("优化PeerUploader：{}", this.torrentSession);
        synchronized (this.peerUploaders) {
            try {
                this.inferiorPeerUploaders();
            } catch (Exception e) {
                LOGGER.error("优化PeerUploader异常", e);
            }
        }
    }
    
    /**
     * 释放资源
     */
    public void release() {
        LOGGER.debug("释放PeerUploaderGroup：{}", this.torrentSession);
        synchronized (this.peerUploaders) {
            this.peerUploaders.forEach(uploader -> SystemThreadContext.submit(uploader::release));
            this.peerUploaders.clear();
        }
    }
    
    /**
     * 剔除劣质Peer
     */
    private void inferiorPeerUploaders() {
        LOGGER.debug("剔除无效PeerUploader：{}", this.torrentSession);
        int index = 0;
        // 有效数量
        int offerSize = 0;
        long uploadMark;
        long downloadMark;
        PeerUploader uploader;
        final int size = this.peerUploaders.size();
        final int maxSize = SystemConfig.getPeerSize();
        while(index++ < size) {
            uploader = this.peerUploaders.poll();
            if(uploader == null) {
                break;
            }
            // 状态无效直接剔除
            if(!uploader.available()) {
                LOGGER.debug("剔除无效PeerUploader（状态无效）");
                this.inferior(uploader);
                continue;
            }
            // 必须获取评分：全部重置
            uploadMark = uploader.uploadMark();
            downloadMark = uploader.downloadMark();
            // 允许连接：提供下载、正在下载
            if(
                downloadMark > 0L ||
                uploader.peerSession().downloading()
            ) {
                offerSize++;
                this.offer(uploader);
                continue;
            }
            if(uploadMark <= 0L) {
                // 没有评分：长时间没有请求的连接
                LOGGER.debug("剔除无效PeerUploader（没有评分）");
                this.inferior(uploader);
            } else if(offerSize > maxSize) {
                LOGGER.debug("剔除无效PeerUploader（超过最大数量）");
                this.inferior(uploader);
            } else {
                offerSize++;
                this.offer(uploader);
            }
        }
    }
    
    /**
     * PeerUploader加入队列
     * 
     * @param peerUploader PeerUploader
     */
    private void offer(PeerUploader peerUploader) {
        if(!this.peerUploaders.offer(peerUploader)) {
            LOGGER.warn("PeerUploader丢失：{}", peerUploader);
        }
    }
    
    /**
     * 剔除劣质PeerUploader
     * 
     * @param peerUploader 劣质PeerUploader
     */
    private void inferior(PeerUploader peerUploader) {
        if(peerUploader != null) {
            LOGGER.debug("剔除无效PeerUploader：{}", peerUploader);
            SystemThreadContext.submit(peerUploader::release);
        }
    }
    
}
