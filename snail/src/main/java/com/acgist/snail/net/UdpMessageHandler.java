package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>UDP消息代理</p>
 * 
 * @author acgist
 */
public abstract class UdpMessageHandler extends MessageHandler<DatagramChannel> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	/**
	 * 远程地址
	 * 
	 * UdpClient                所有客户端固定地址
	 * LSD/Stun/UPNP/Tracker    只要接收的服务端不用管理地址
	 * UTP/Quick                主动发送的服务端单独管理消息代理
	 * DHT                      被动发送的服务端直接使用消息地址
	 */
	protected final InetSocketAddress socketAddress;
	
	/**
	 * @param socketAddress 远程地址
	 */
	protected UdpMessageHandler(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	@Override
	public void handle(DatagramChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	@Override
	public void close() {
		LOGGER.debug("UDP连接关闭：{}", this.socketAddress);
		// 标记关闭：不能关闭通道（UDP通道单例复用）
		this.close = true;
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected final void send(ByteBuffer buffer, SocketAddress socketAddress) throws NetException {
		this.check(buffer);
		try {
			// UDP不用加锁
			final int size = this.channel.send(buffer, socketAddress);
			if(size <= 0) {
				LOGGER.warn("UDP消息发送失败：{}-{}", socketAddress, size);
			}
		} catch (IOException e) {
			throw new NetException(e);
		}
	}
	
}
