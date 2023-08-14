package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.TcpClient;

/**
 * Peer客户端
 * 
 * @author acgist
 */
public final class PeerClient extends TcpClient<PeerMessageHandler> {

    /**
     * Peer信息
     */
    private final PeerSession peerSession;
    /**
     * Peer消息代理
     */
    private final PeerSubMessageHandler peerSubMessageHandler;
    
    /**
     * @param peerSession           Peer信息
     * @param peerSubMessageHandler Peer消息代理
     */
    private PeerClient(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
        super("Peer Client", SystemConfig.CONNECT_TIMEOUT, new PeerMessageHandler(peerSubMessageHandler));
        this.peerSession           = peerSession;
        this.peerSubMessageHandler = peerSubMessageHandler;
    }

    /**
     * 新建Peer客户端
     * 
     * @param peerSession           Peer信息
     * @param peerSubMessageHandler Peer消息代理
     * 
     * @return {@link PeerClient}
     */
    public static final PeerClient newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
        return new PeerClient(peerSession, peerSubMessageHandler);
    }
    
    @Override
    public boolean connect() {
        return this.connect(this.peerSession.host(), this.peerSession.port());
    }

    /**
     * @return Peer信息
     */
    public PeerSession getPeerSession() {
        return this.peerSession;
    }
    
    /**
     * @return Peer消息代理
     */
    public PeerSubMessageHandler getPeerSubMessageHandler() {
        return this.peerSubMessageHandler;
    }
    
}
