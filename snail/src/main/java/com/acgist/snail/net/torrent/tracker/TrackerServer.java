package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.net.UdpServer;

/**
 * <p>Tracker服务端</p>
 * 
 * @author acgist
 */
public final class TrackerServer extends UdpServer<TrackerAcceptHandler> {

	private static final TrackerServer INSTANCE = new TrackerServer();
	
	public static final TrackerServer getInstance() {
		return INSTANCE;
	}
	
	private TrackerServer() {
		super("Tracker Server", TrackerAcceptHandler.getInstance());
		this.handle();
	}

}
