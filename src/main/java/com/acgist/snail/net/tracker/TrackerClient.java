package com.acgist.snail.net.tracker;

import com.acgist.snail.net.UdpClient;

/**
 * <p>Tracker Client</p>
 * <p>UDP协议、随机端口。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerClient extends UdpClient<TrackerMessageHandler> {

	private static final TrackerClient INSTANCE = new TrackerClient();

	static {
		UdpClient.bindServerHandler(new TrackerMessageHandler(), INSTANCE.channel);
	}
	
	private TrackerClient() {
		super("Tracker Client", new TrackerMessageHandler());
		this.open();
	}
	
	public static final TrackerClient getInstance() {
		return INSTANCE;
	}
	
}
