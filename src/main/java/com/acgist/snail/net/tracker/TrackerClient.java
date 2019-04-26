package com.acgist.snail.net.tracker;

import com.acgist.snail.net.UdpClient;

/**
 * tracker client
 */
public class TrackerClient extends UdpClient<TrackerMessageHandler> {

	private static final TrackerClient INSTANCE = new TrackerClient();

	private TrackerClient() {
		super("Tracker Client", new TrackerMessageHandler());
		this.open();
		this.handle();
	}
	
	public static final TrackerClient getInstance() {
		return INSTANCE;
	}

}
