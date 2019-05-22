package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>UDP客户端</p>
 * <p>UDP客户端、服务端通道都是一个。</p>
 * <p>
 * 实现（消息处理）：
 * <ul>
 * 	<li>单例</li>
 * 	<li>UDP通道单例</li>
 * </ul>
 * </p>
 * 
 * TODO：超时
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends UdpSender {
	
	public static final String UDP_REGEX = "udp://.*";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	
	/**
	 * 请求地址
	 */
	protected final InetSocketAddress address;
	
	/**
	 * 消息代理
	 */
	protected final T handler;
	
	public UdpClient(String name, T handler, InetSocketAddress address) {
		this.name = name;
		this.handler = handler;
		this.address = address;
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
		this.channel = channel;
		this.handler.handle(this.channel);
		return true;
	}
	
	/**
	 * 多播分组
	 */
	public void join(String group) {
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 2);
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
			this.channel.join(InetAddress.getByName(group), NetUtils.defaultNetworkInterface());
		} catch (IOException e) {
			LOGGER.info("UDP多播异常：{}", group, e);
		}
	}
	
	protected void send(final String message) throws NetException {
		this.send(message, this.address);
	}
	
	protected void send(byte[] bytes) throws NetException {
		this.send(bytes, this.address);
	}
	
	public void send(ByteBuffer buffer) throws NetException {
		this.send(buffer, this.address);
	}
	
	/**
	 * 关闭channel，所有的UDP通道都是单例，系统关闭时调用。调用UdpServer.close()。
	 */
	@Deprecated
	public void close() {
		LOGGER.debug("UDP Client关闭：{}", this.name);
		IoUtils.close(this.channel);
	}

	/**
	 * 验证UCP协议
	 */
	public static final boolean verify(String url) {
		return StringUtils.regex(url, UDP_REGEX, true);
	}

}
