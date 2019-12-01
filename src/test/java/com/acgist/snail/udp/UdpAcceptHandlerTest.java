package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

public class UdpAcceptHandlerTest extends UdpAcceptHandler {

	private static final UdpAcceptHandlerTest INSTANCE = new UdpAcceptHandlerTest();
	
	private UdpAcceptHandlerTest() {
	}
	
	public static final UdpAcceptHandlerTest getInstance() {
		return INSTANCE;
	}
	
	private final UdpMessageHandlerTest udpTestMessageHandler = new UdpMessageHandlerTest();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.udpTestMessageHandler;
	}

}
