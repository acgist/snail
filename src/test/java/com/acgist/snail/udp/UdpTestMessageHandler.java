package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.StringUtils;

public class UdpTestMessageHandler extends UdpMessageHandler {

	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		System.out.println(socketAddress + "-" + socketAddress.getClass());
		final String content = StringUtils.readContent(buffer);
		System.out.println("收到消息：" + content);
		System.out.println("消息长度：" + content.length());
	}

}
