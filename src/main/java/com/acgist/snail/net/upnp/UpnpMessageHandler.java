package com.acgist.snail.net.upnp;

import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;

/**
 * UPNP消息
 */
public class UpnpMessageHandler extends UdpMessageHandler {

	@Override
	public void doMessage(ByteBuffer buffer) {
		System.out.println(new String(buffer.array()));
	}

}
