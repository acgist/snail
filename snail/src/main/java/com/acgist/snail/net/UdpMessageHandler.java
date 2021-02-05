package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;

/**
 * <p>UDP消息代理</p>
 * 
 * @author acgist
 */
public abstract class UdpMessageHandler implements IMessageSender, IMessageReceiver, IChannelHandler<DatagramChannel> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	/**
	 * <p>是否关闭</p>
	 */
	protected volatile boolean close = false;
	/**
	 * <p>UDP通道</p>
	 */
	protected DatagramChannel channel;
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
	 * <p>消息处理器</p>
	 */
	protected IMessageDecoder<ByteBuffer> messageDecoder;
	
	/**
	 * @param socketAddress 远程地址
	 */
	protected UdpMessageHandler(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(this.messageDecoder == null) {
			throw new NetException("请设置消息处理器或重新接收消息方法");
		}
		this.messageDecoder.decode(buffer, socketAddress);
	}
	
	@Override
	public void handle(DatagramChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public boolean available() {
		// 不用判断状态：使用服务通道
		return !this.close && this.channel != null;
	}
	
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>标记关闭：不能关闭通道（UDP通道单例复用）</p>
	 */
	@Override
	public void close() {
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
