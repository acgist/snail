package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

public class UdpTestAcceptHandler extends UdpAcceptHandler {

	private static final UdpTestAcceptHandler INSTANCE = new UdpTestAcceptHandler();
	
	private UdpTestAcceptHandler() {
	}
	
	public static final UdpTestAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private final UdpTestMessageHandler udpTestMessageHandler = new UdpTestMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.udpTestMessageHandler;
	}

}
