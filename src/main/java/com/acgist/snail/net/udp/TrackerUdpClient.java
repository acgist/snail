package com.acgist.snail.net.udp;

import com.acgist.snail.net.AbstractUdpClient;
import com.acgist.snail.net.message.impl.TrackerMessageHandler;

/**
 * tracker udp client
 */
public class TrackerUdpClient extends AbstractUdpClient<TrackerMessageHandler> {

	private static final TrackerUdpClient INSTANCE = new TrackerUdpClient();

	private TrackerUdpClient() {
		this.open();
		this.bindMessageHandler(new TrackerMessageHandler());
	}
	
	public static final TrackerUdpClient getInstance() {
		return INSTANCE;
	}

}
