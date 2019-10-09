package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP消息接收代理
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpAcceptHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpAcceptHandler.class);

	/**
	 * 消息代理
	 */
	public void handle(DatagramChannel channel, ByteBuffer buffer, InetSocketAddress socketAddress) {
		final UdpMessageHandler handler = messageHandler(buffer, socketAddress);
		try {
			handler.handle(channel, socketAddress); // 设置代理
			if(handler.available()) {
				handler.onReceive(buffer, socketAddress);
			}
		} catch (Exception e) {
			LOGGER.error("UDP消息接收异常", e);
		}
	}
	
	/**
	 * 获取消息代理
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 * 
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress);
	
}
