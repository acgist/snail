package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Local Service Discovery</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0014.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class LocalServiceDiscoveryMessageHandler extends UdpMessageHandler {

	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
	}

}
