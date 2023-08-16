package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.TcpServer;

/**
 * Peer服务端
 * 
 * @author acgist
 */
public final class PeerServer extends TcpServer<PeerMessageHandler> {
    
    private static final PeerServer INSTANCE = new PeerServer();
    
    public static final PeerServer getInstance() {
        return INSTANCE;
    }
    
    private PeerServer() {
        super("Peer Server", PeerMessageHandler.class);
        this.listen();
    }
    
    @Override
    public boolean listen() {
        return this.listen(SystemConfig.getTorrentPort());
    }

}
