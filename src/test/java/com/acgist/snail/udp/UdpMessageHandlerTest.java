package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.StringUtils;

public class UdpMessageHandlerTest extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandlerTest.class);
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		LOGGER.debug(socketAddress + "-" + socketAddress.getClass());
		final String content = StringUtils.ofByteBuffer(buffer);
		LOGGER.debug("收到消息：" + content);
		LOGGER.debug("消息长度：" + content.length());
	}

}
