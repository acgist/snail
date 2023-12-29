package com.acgist.snail.net.torrent.utp;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;

/**
 * UTP客户端
 * 
 * @author acgist
 */
public final class UtpClient extends UdpClient<UtpMessageHandler> {

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
    private UtpClient(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
        super("UTP Client", new UtpMessageHandler(peerSubMessageHandler, peerSession.peerSocketAddress()));
        this.peerSession = peerSession;
        this.peerSubMessageHandler = peerSubMessageHandler;
    }
    
    /**
     * 新建UTP客户端
     * 
     * @param peerSession           Peer信息
     * @param peerSubMessageHandler Peer消息代理
     * 
     * @return UTP客户端
     */
    public static final UtpClient newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
        return new UtpClient(peerSession, peerSubMessageHandler);
    }
    
    @Override
    public boolean open() {
        return open(TorrentServer.getInstance().getChannel());
    }
    
    /**
     * 连接
     * 
     * @return 是否连接成功
     */
    public boolean connect() {
        return this.handler.connect();
    }

    /**
     * @return Peer信息
     */
    public PeerSession peerSession() {
        return this.peerSession;
    }
    
    /**
     * @return Peer消息代理
     */
    public PeerSubMessageHandler peerSubMessageHandler() {
        return this.peerSubMessageHandler;
    }
    
}
