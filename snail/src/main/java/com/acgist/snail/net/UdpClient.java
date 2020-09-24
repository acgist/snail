package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>UDP客户端</p>
 * <p>UDP客户端、服务端通道都是公用一个，程序关闭时关闭服务才关闭通道。</p>
 * <ul>
 * 	<li>单例</li>
 * 	<li>UDP通道使用服务器通道</li>
 * </ul>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends ClientMessageHandlerAdapter<T> implements IUdpChannel {
	
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
	 * <p>新建客户端</p>
	 * 
	 * @param name 客户端名称
	 * @param handler 消息处理器
	 * @param socketAddress 远程地址
	 */
	public UdpClient(String name, T handler, InetSocketAddress socketAddress) {
		super(handler);
		this.name = name;
		this.socketAddress = socketAddress;
		this.open();
	}

	/**
	 * <p>打开客户端</p>
	 * <p>随机端口</p>
	 * 
	 * @return 打开状态
	 */
	public abstract boolean open();
	
	/**
	 * <p>打开客户端</p>
	 * 
	 * @param port 端口
	 * 
	 * @return 打开状态
	 */
	public boolean open(final int port) {
		DatagramChannel channel = null;
		try {
			channel = this.buildUdpChannel(port);
		} catch (NetException e) {
			LOGGER.error("打开UDP客户端异常", e);
		}
		return this.open(channel);
	}

	/**
	 * <p>打开客户端</p>
	 * <p>客户端和服务端的使用同一个通道</p>
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
	 * <p>关闭资源</p>
	 * <p>标记关闭：不能关闭通道（UDP通道单例复用）</p>
	 */
	@Override
	public void close() {
		LOGGER.debug("关闭UDP Client：{}", this.name);
		super.close();
	}

}
