package com.acgist.snail.net.tracker;

import com.acgist.snail.net.UdpClient;

/**
 * tracker client
 */
public class TrackerUdpClient extends UdpClient<TrackerMessageHandler> {

	private static final TrackerUdpClient INSTANCE = new TrackerUdpClient();

	private TrackerUdpClient() {
		super("Tracker Client", new TrackerMessageHandler());
		this.open();
		this.bindMessageHandler();
	}
	
	public static final TrackerUdpClient getInstance() {
		return INSTANCE;
	}

}
