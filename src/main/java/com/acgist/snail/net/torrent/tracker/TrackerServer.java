package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.net.UdpServer;

/**
 * <p>Tracker服务端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerServer extends UdpServer<TrackerAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerServer.class);
	
	private TrackerServer() {
		super(-1, "Tracker Server", TrackerAcceptHandler.getInstance());
		this.handle();
	}
	
	private static final TrackerServer INSTANCE = new TrackerServer();
	
	public static final TrackerServer getInstance() {
		return INSTANCE;
	}

}
