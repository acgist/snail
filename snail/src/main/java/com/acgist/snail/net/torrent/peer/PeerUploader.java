package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;

/**
 * Peer接入
 * 
 * @author acgist
 */
public final class PeerUploader extends PeerConnect {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerUploader.class);

    /**
     * @param peerSession           Peer信息
     * @param torrentSession        BT任务信息
     * @param peerSubMessageHandler Peer消息代理
     */
    private PeerUploader(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
        super(peerSession, torrentSession, peerSubMessageHandler);
        this.available = true;
    }
    
    /**
     * 新建Peer接入
     * 
     * @param peerSession           Peer信息
     * @param torrentSession        BT任务信息
     * @param peerSubMessageHandler Peer消息代理
     * 
     * @return {@link PeerUploader}
     */
    public static final PeerUploader newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
        return new PeerUploader(peerSession, torrentSession, peerSubMessageHandler);
    }

    @Override
    public void download() {
        if(
            // 快速允许
            this.peerSession.supportAllowedFast() ||
            // 解除阻塞
            this.peerConnectSession.isPeerUnchoked()
        ) {
            super.download();
        }
    }

    @Override
    public void release() {
        try {
            if(this.available) {
                LOGGER.debug("关闭PeerUploader：{}", this.peerSession);
                super.release();
            }
        } catch (Exception e) {
            LOGGER.error("关闭PeerUploader异常", e);
        } finally {
            this.peerSession.statusOff(PeerConfig.STATUS_UPLOAD);
            this.peerSession.peerUploader(null);
        }
    }
    
}
