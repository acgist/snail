package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * UDP Tracker消息接收代理
 * 
 * @author acgist
 */
public final class TrackerAcceptHandler extends UdpAcceptHandler {
    
    private static final TrackerAcceptHandler INSTANCE = new TrackerAcceptHandler();
    
    public static final TrackerAcceptHandler getInstance() {
        return INSTANCE;
    }
    
    private TrackerAcceptHandler() {
    }
    
    /**
     * 消息代理
     */
    private final TrackerMessageHandler trackerMessageHandler = new TrackerMessageHandler();
    
    @Override
    public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
        return this.trackerMessageHandler;
    }

    @Override
    public void handle(DatagramChannel channel) {
        this.trackerMessageHandler.handle(channel);
    }
    
}
