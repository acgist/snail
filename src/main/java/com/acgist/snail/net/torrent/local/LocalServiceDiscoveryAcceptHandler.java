package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * 本地发现接收器
 * 
 * @author acgist
 * @since 1.1.0
 */
public class LocalServiceDiscoveryAcceptHandler extends UdpAcceptHandler {

	private static final LocalServiceDiscoveryAcceptHandler INSTANCE = new LocalServiceDiscoveryAcceptHandler();
	
	private LocalServiceDiscoveryAcceptHandler() {
	}
	
	public static final LocalServiceDiscoveryAcceptHandler getInstance() {
		return INSTANCE;
	}

	private final LocalServiceDiscoveryMessageHandler localServiceDiscoveryMessageHandler = new LocalServiceDiscoveryMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.localServiceDiscoveryMessageHandler;
	}

}
