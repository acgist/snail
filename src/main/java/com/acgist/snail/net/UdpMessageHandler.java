package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * UDP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpMessageHandler extends UdpSender {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	protected boolean server = false; // 是否是服务端
	
	/**
	 * 消息处理
	 */
	public abstract void onMessage(ByteBuffer buffer, InetSocketAddress address);
	
	/**
	 * 代理Channel
	 */
	public void handle(DatagramChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * 设置为服务端
	 */
	public UdpMessageHandler server() {
		this.server = true;
		return this;
	}
	
	/**
	 * 判断是否是服务端
	 */
	public boolean isServer() {
		return this.server;
	}

}
