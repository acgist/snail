package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * <p>UDP Tracker消息接收代理</p>
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
	 * <p>重复使用消息代理</p>
	 */
	private final TrackerMessageHandler trackerMessageHandler = new TrackerMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.trackerMessageHandler;
	}

}
