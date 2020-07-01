package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * <p>UPNP接收器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class UpnpAcceptHandler extends UdpAcceptHandler {

	private static final UpnpAcceptHandler INSTANCE = new UpnpAcceptHandler();
	
	private UpnpAcceptHandler() {
	}
	
	public static final UpnpAcceptHandler getInstance() {
		return INSTANCE;
	}

	private final UpnpMessageHandler upnpMessageHandler = new UpnpMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.upnpMessageHandler;
	}

}
