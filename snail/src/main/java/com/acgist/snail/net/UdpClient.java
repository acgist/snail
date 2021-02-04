package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>UDP客户端</p>
 * <ul>
 * 	<li>单例</li>
 * 	<li>UDP通道使用服务器通道</li>
 * </ul>
 * 
 * @param <T> UDP消息代理
 * 
 * @author acgist
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends ClientMessageHandlerAdapter<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * <p>客户端名称</p>
	 */
	private final String name;
	/**
	 * <p>远程地址</p>
	 */
	protected final InetSocketAddress socketAddress;
	
	/**
	 * <p>创建客户端时自动打开通道</p>
	 * 
	 * @param name 客户端名称
	 * @param handler 消息代理
	 * @param socketAddress 远程地址
	 */
	public UdpClient(String name, T handler, InetSocketAddress socketAddress) {
		super(handler);
		this.name = name;
		this.socketAddress = socketAddress;
		this.open();
	}

	/**
	 * <p>打开通道</p>
	 * 
	 * @return 打开状态
	 */
	public abstract boolean open();
	
	/**
	 * <p>打开通道</p>
	 * 
	 * @param channel 通道
	 * 
	 * @return 打开状态
	 */
	public boolean open(DatagramChannel channel) {
		if(channel == null) {
			return false;
		}
		this.handler.handle(channel, this.socketAddress);
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>标记关闭：不能关闭通道（UDP通道单例复用）</p>
	 */
	@Override
	public void close() {
		LOGGER.debug("关闭UDP Client：{}", this.name);
		super.close();
	}

}
