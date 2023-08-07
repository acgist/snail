package com.acgist.snail.net.torrent.lsd;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * 本地发现消息接收代理
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryAcceptHandler extends UdpAcceptHandler {

    private static final LocalServiceDiscoveryAcceptHandler INSTANCE = new LocalServiceDiscoveryAcceptHandler();
    
    public static final LocalServiceDiscoveryAcceptHandler getInstance() {
        return INSTANCE;
    }
    
    /**
     * 消息代理
     */
    private final LocalServiceDiscoveryMessageHandler localServiceDiscoveryMessageHandler = new LocalServiceDiscoveryMessageHandler();
    
    private LocalServiceDiscoveryAcceptHandler() {
    }
    
    @Override
    public void handle(DatagramChannel channel) {
        this.localServiceDiscoveryMessageHandler.handle(channel);
    }
    
    @Override
    public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
        return this.localServiceDiscoveryMessageHandler;
    }

}
