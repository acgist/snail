package com.acgist.main;

import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.IoUtils;

public class UdpTestMessageHandler extends UdpMessageHandler {

	public UdpTestMessageHandler() {
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) {
		System.out.println("收到消息：" + IoUtils.readContent(buffer));
	}
	
}
