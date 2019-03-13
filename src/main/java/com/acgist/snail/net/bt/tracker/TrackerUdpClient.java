package com.acgist.snail.net.bt.tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * tracker udp 客户端
 */
public class TrackerUdpClient {

	public static void main(String[] args) {
	}

	public void decode(String trackerUrl) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(9999));
	}

}
