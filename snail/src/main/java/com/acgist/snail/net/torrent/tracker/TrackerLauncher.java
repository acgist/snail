package com.acgist.snail.net.torrent.tracker;

import java.util.Map;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * Tracker执行器
 * 使用TrackerSession查询Peer信息
 * 
 * @author acgist
 */
public final class TrackerLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
    
    /**
     * transaction_id
     * 对应Tracker服务器和BT任务信息
     */
    private final Integer id;
    /**
     * 可用状态
     */
    private boolean available = true;
    /**
     * 是否需要释放
     */
    private boolean needRelease = false;
    /**
     * Tracker信息
     */
    private final TrackerSession session;
    /**
     * BT任务信息
     */
    private final TorrentSession torrentSession;
    
    /**
     * @param session        Tracker信息
     * @param torrentSession BT任务信息
     */
    private TrackerLauncher(TrackerSession session, TorrentSession torrentSession) {
        this.id = NumberUtils.build();
        this.session = session;
        this.torrentSession = torrentSession;
    }
    
    /**
     * 新建TrackerLauncher
     * 
     * @param session        Tracker信息
     * @param torrentSession BT任务信息
     * 
     * @return {@link TrackerLauncher}
     */
    public static final TrackerLauncher newInstance(TrackerSession session, TorrentSession torrentSession) {
        return new TrackerLauncher(session, torrentSession);
    }

    /**
     * @return ID
     */
    public Integer id() {
        return this.id;
    }
    
    /**
     * @return 声明地址
     */
    public String announceUrl() {
        return this.session.announceUrl();
    }

    /**
     * 查找Peer
     */
    public void findPeer() {
        if(this.available()) {
            LOGGER.debug("TrackerLauncher查找Peer：{}", this.session);
            this.needRelease = true;
            this.session.findPeers(this.id, this.torrentSession);
        }
    }

    /**
     * 收到声明响应消息
     * 
     * @param message 声明响应消息
     */
    public void announce(AnnounceMessage message) {
        if(message == null) {
            return;
        }
        if(this.available()) {
            this.peer(message.peers());
        } else {
            LOGGER.debug("收到声明响应消息（TrackerLauncher无效）：{}", this.session);
        }
    }
    
    /**
     * 添加Peer
     * 
     * @param peers Peer列表
     */
    private void peer(Map<String, Integer> peers) {
        if(MapUtils.isEmpty(peers)) {
            return;
        }
        final PeerContext peerContext = PeerContext.getInstance();
        peers.forEach((host, port) -> peerContext.newPeerSession(
            this.torrentSession.infoHashHex(),
            this.torrentSession.statistics(),
            host,
            port,
            PeerConfig.Source.TRACKER
        ));
    }

    /**
     * 释放资源
     */
    public void release() {
        if(this.needRelease && this.available()) {
            this.needRelease = false;
            try {
                if(this.torrentSession.completed()) {
                    LOGGER.debug("TrackerLauncher完成通知：{}", this.session);
                    this.session.completed(this.id, this.torrentSession);
                } else {
                    LOGGER.debug("TrackerLauncher暂停通知：{}", this.session);
                    this.session.stopped(this.id, this.torrentSession);
                }
            } catch (Exception e) {
                LOGGER.error("TrackerLauncher关闭异常", e);
            } finally {
                TrackerContext.getInstance().removeTrackerLauncher(this.id);
            }
        }
        // 立即标记释放资源
        this.available = false;
    }
    
    /**
     * 判断是否可用
     * 
     * @return 是否可用
     * 
     * @see #available
     * @see TrackerSession#available()
     */
    private boolean available() {
        return this.available && this.session.available();
    }
    
}
