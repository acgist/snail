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
	 * <p>远程地址</p>
	 * <table border="1">
	 * 	<caption>远程地址管理</caption>
	 * 	<tr>
	 * 		<th>类型</th>
	 * 		<th>描述</th>
	 * 		<th>参考</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>客户端</td>
	 * 		<td>初始化固定地址</td>
	 * 		<td>UdpClient子类</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>服务端（只要接收）</td>
	 * 		<td>不用管理地址</td>
	 * 		<td>LSD/Stun/UPNP/Tracker</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>服务端（需要发送）</td>
	 * 		<td>单独管理消息代理</td>
	 * 		<td>UTP</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>服务端（需要发送）</td>
	 * 		<td>没有管理消息代理：直接使用消息地址</td>
	 * 		<td>DHT</td>
	 * 	</tr>
	 * </table>
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
