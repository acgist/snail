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
		final String content = IoUtils.readContent(buffer);
		System.out.println("收到消息：" + content);
		System.out.println("消息长度：" + content.length());
	}

}
