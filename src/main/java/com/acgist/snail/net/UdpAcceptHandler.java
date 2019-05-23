package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * 消息接收器。
 */
public abstract class UdpAcceptHandler {

	/**
	 * 消息处理
	 */
	public void handler(ByteBuffer buffer, InetSocketAddress address) {
		messageHandler(buffer, address).onMessage(buffer, address);
	}
	
	/**
	 * 获取消息代理
	 * 
	 * @param buffer 消息
	 * @param address 地址
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress address);
	
}
