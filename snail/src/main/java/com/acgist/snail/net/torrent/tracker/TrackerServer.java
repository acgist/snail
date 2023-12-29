package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.net.UdpServer;

/**
 * UDP Tracker服务端
 * 
 * @author acgist
 */
public final class TrackerServer extends UdpServer<TrackerAcceptHandler> {

    private static final TrackerServer INSTANCE = new TrackerServer();
    
    public static final TrackerServer getInstance() {
        return INSTANCE;
    }
    
    private TrackerServer() {
        super("Tracker Server", TrackerAcceptHandler.getInstance());
        this.handle();
    }

}
