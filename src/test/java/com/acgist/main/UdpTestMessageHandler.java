package com.acgist.main;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.IoUtils;

public class UdpTestMessageHandler extends UdpMessageHandler {

	public UdpTestMessageHandler() {
	}
	
	@Override
	public void onMessage(InetSocketAddress address, ByteBuffer buffer) {
		System.out.println(address + "-" + address.getClass());
		System.out.println("收到消息：" + IoUtils.readContent(buffer));
	}
	
}
