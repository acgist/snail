package com.acgist.snail.net.tracker;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;

/**
 * <p>Tracker Client</p>
 * <p>UDP协议、随机端口。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerClient extends UdpClient<TrackerMessageHandler> {

	private TrackerClient(InetSocketAddress socketAddress) {
		super("Tracker Client", new TrackerMessageHandler(), socketAddress);
	}
	
	public static final TrackerClient newInstance(InetSocketAddress socketAddress) {
		return new TrackerClient(socketAddress);
	}

	@Override
	public boolean open() {
		return this.open(TrackerServer.getInstance().channel());
	}

}
