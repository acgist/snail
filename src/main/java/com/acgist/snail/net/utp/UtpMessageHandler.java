package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;

/**
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress address) {
		
	}
	
}
