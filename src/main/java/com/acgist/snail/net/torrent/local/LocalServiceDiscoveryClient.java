package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;

/**
 * <p>本地发现客户端</p>
 * <p>基本协议：UDP</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class LocalServiceDiscoveryClient extends UdpClient<LocalServiceDiscoveryMessageHandler> {

	public LocalServiceDiscoveryClient(InetSocketAddress socketAddress) {
		super("Local Service Discovery Client", new LocalServiceDiscoveryMessageHandler(), socketAddress);
	}

	@Override
	public boolean open() {
		return false;
	}
	
}
