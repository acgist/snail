package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;

/**
 * <p>Tracker Client（UDP）</p>
 * 
 * @author acgist
 */
public final class TrackerClient extends UdpClient<TrackerMessageHandler> {

	/**
	 * @param socketAddress 地址
	 */
	private TrackerClient(InetSocketAddress socketAddress) {
		super("Tracker Client", new TrackerMessageHandler(socketAddress));
	}
	
	/**
	 * <p>创建Tracker客户端</p>
	 * 
	 * @param socketAddress 地址
	 * 
	 * @return Tracker客户端
	 */
	public static final TrackerClient newInstance(InetSocketAddress socketAddress) {
		return new TrackerClient(socketAddress);
	}

	@Override
	public boolean open() {
		return this.open(TrackerServer.getInstance().channel());
	}
	
}
