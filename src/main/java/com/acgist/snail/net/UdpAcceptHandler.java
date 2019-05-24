package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息接收器。
 */
public abstract class UdpAcceptHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpAcceptHandler.class);
	
	/**
	 * 消息处理
	 */
	public void handle(DatagramChannel channel, ByteBuffer buffer, InetSocketAddress socketAddress) {
		final UdpMessageHandler handler = messageHandler(buffer, socketAddress);
		synchronized (handler) {
			try {
				handler.handle(channel, socketAddress).onMessage(buffer, socketAddress);
			} catch (Exception e) {
				LOGGER.error("TCP消息处理异常", e);
			}
		}
	}
	
	/**
	 * 获取消息代理
	 * 
	 * @param buffer 消息
	 * @param address 地址
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress);
	
}
