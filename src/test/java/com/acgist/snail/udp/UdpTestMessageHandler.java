package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.IoUtils;

public class UdpTestMessageHandler extends UdpMessageHandler {

	public UdpTestMessageHandler() {
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		System.out.println(socketAddress + "-" + socketAddress.getClass());
		final String content = IoUtils.readContent(buffer);
		System.out.println("收到消息：" + content);
		System.out.println("消息长度：" + content.length());
	}

}
