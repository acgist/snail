package com.acgist.snail.net.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * UDP Tracker接收器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerAcceptHandler extends UdpAcceptHandler {
	
	private static final TrackerAcceptHandler INSTANCE = new TrackerAcceptHandler();
	
	private TrackerAcceptHandler() {
	}
	
	public static final TrackerAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private TrackerMessageHandler trackerMessageHandler = new TrackerMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return trackerMessageHandler;
	}

}
