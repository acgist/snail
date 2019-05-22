package com.acgist.snail.net.tracker;

import com.acgist.snail.net.UdpServer;

public class TrackerServer extends UdpServer<TrackerMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerServer.class);
	
	private TrackerServer() {
		super(-1, "Tracker Server", TrackerMessageHandler.class);
		this.handler();
	}
	
	private static final TrackerServer INSTANCE = new TrackerServer();
	
	public static final TrackerServer getInstance() {
		return INSTANCE;
	}

}
