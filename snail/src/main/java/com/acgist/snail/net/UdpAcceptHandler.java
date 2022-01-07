package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>UDP消息接收代理</p>
 * 
 * @author acgist
 */
public abstract class UdpAcceptHandler implements IChannelHandler<DatagramChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpAcceptHandler.class);

	/**
	 * <p>接收消息</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 */
	public void receive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final UdpMessageHandler handler = this.messageHandler(buffer, socketAddress);
		try {
			if(handler.available()) {
				buffer.flip();
				handler.onReceive(buffer, socketAddress);
			}
		} catch (Exception e) {
			LOGGER.error("UDP接收消息异常：{}", socketAddress, e);
		}
	}
	
	/**
	 * <p>获取消息代理</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 * 
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress);
	
}
