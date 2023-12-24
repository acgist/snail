package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * UPNP消息接收代理
 * 
 * @author acgist
 */
public final class UpnpAcceptHandler extends UdpAcceptHandler {

    private static final UpnpAcceptHandler INSTANCE = new UpnpAcceptHandler();
    
    public static final UpnpAcceptHandler getInstance() {
        return INSTANCE;
    }
    
    /**
     * 消息代理
     */
    private final UpnpMessageHandler upnpMessageHandler = new UpnpMessageHandler();
    
    private UpnpAcceptHandler() {
    }

    @Override
    public void handle(DatagramChannel channel) {
        this.upnpMessageHandler.handle(channel);
    }
    
    @Override
    public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
        return this.upnpMessageHandler;
    }

}
