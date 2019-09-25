package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP客户端</p>
 * <p>UDP客户端、服务端通道都是一个。</p>
 * <p>
 * 实现（消息处理）：
 * <ul>
 * 	<li>单例</li>
 * 	<li>UDP通道使用服务器通道</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends ClientMessageHandlerAdapter<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	/**
	 * 发送地址
	 */
	protected final InetSocketAddress socketAddress;
	
	/**
	 * 新建客户端
	 * 
	 * @param name 客户端名称
	 * @param handler 消息处理器，每一个客户的必须唯一
	 * @param socketAddress 远程客户端地址
	 */
	public UdpClient(String name, T handler, InetSocketAddress socketAddress) {
		super(handler);
		this.name = name;
		this.socketAddress = socketAddress;
		this.open();
	}

	/**
	 * 打开客户端，随机端口
	 */
	public abstract boolean open();
	
	/**
	 * 打开客户端
	 */
	public boolean open(final int port) {
		return this.open(null, port);
	}

	/**
	 * 打开客户端
	 */
	public boolean open(final String host, final int port) {
		final DatagramChannel channel = NetUtils.buildUdpChannel(host, port);
		return open(channel);
	}
	
	/**
	 * 打开客户端：客户端和服务的使用同一个端口
	 */
	public boolean open(DatagramChannel channel) {
		if(channel == null) {
			return false;
		}
		this.handler.handle(channel, this.socketAddress);
		return true;
	}
	
	/**
	 * 关闭资源，标记关闭，不能关闭通道。UDP通道只打开一个，程序结束时才能关闭。
	 */
	public void close() {
		LOGGER.debug("UDP Client关闭：{}", this.name);
		super.close();
	}

}
