package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * <p>UPNP消息接收代理</p>
 * 
 * @author acgist
 */
public final class UpnpAcceptHandler extends UdpAcceptHandler {

	private static final UpnpAcceptHandler INSTANCE = new UpnpAcceptHandler();
	
	public static final UpnpAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private UpnpAcceptHandler() {
	}

	/**
	 * <p>UPNP消息代理</p>
	 */
	private final UpnpMessageHandler upnpMessageHandler = new UpnpMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.upnpMessageHandler;
	}

}
