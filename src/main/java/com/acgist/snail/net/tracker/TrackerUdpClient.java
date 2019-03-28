package com.acgist.snail.net.tracker;

import com.acgist.snail.net.UdpClient;

/**
 * tracker udp client
 */
public class TrackerUdpClient extends UdpClient<TrackerMessageHandler> {

	private static final TrackerUdpClient INSTANCE = new TrackerUdpClient();

	private TrackerUdpClient() {
		this.open();
		this.bindMessageHandler(new TrackerMessageHandler());
	}
	
	public static final TrackerUdpClient getInstance() {
		return INSTANCE;
	}

}
