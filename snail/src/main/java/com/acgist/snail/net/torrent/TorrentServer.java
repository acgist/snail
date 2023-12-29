package com.acgist.snail.net.torrent;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.UdpServer;

/**
 * Torrent服务端：UTP、DHT、STUN
 * 
 * @author acgist
 */
public final class TorrentServer extends UdpServer<TorrentAcceptHandler> {
    
    private static final TorrentServer INSTANCE = new TorrentServer();
    
    public static final TorrentServer getInstance() {
        return INSTANCE;
    }

    private TorrentServer() {
        super(SystemConfig.getTorrentPort(), "Torrent(UTP/DHT/STUN) Server", TorrentAcceptHandler.getInstance());
        this.handle();
    }

}
