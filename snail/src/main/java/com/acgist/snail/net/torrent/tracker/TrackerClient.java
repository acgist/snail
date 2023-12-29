package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;

/**
 * UDP Tracker客户端
 * 
 * @author acgist
 */
public final class TrackerClient extends UdpClient<TrackerMessageHandler> {

    /**
     * @param socketAddress 地址
     */
    private TrackerClient(InetSocketAddress socketAddress) {
        super("Tracker Client", new TrackerMessageHandler(socketAddress));
    }
    
    /**
     * 新建Tracker客户端
     * 
     * @param socketAddress 地址
     * 
     * @return {@link TrackerClient}
     */
    public static final TrackerClient newInstance(InetSocketAddress socketAddress) {
        return new TrackerClient(socketAddress);
    }

    @Override
    public boolean open() {
        return this.open(TrackerServer.getInstance().getChannel());
    }
    
}
