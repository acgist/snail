package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * UDP客户端
 * UDP客户端、服务端通道都是一个
 * 实现（消息处理）：
 * 	1.单例
 * 	2.UDP通道单例
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends UdpSender {
	
	public static final String UDP_REGEX = "udp://.*";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	
	protected final T handler;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_CLIENT);
	}
	
	public UdpClient(String name, T handler) {
		this.name = name;
		this.handler = handler;
	}

	/**
	 * 打开客户端，随机端口
	 */
	public boolean open() {
		return this.open(-1);
	}
	
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
			this.channel.join(InetAddress.getByName(group), NetUtils.defaultNetworkInterface());
		} catch (IOException e) {
			LOGGER.info("UDP多播异常：{}", group, e);
		}
	}
	
	/**
	 * 关闭channel，所有的UDP通道都是单例，系统关闭时调用
	 */
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

	/**
	 * 绑定消息处理
	 */
	public static final <T extends UdpMessageHandler> void bindServerHandler(final T handler, final DatagramChannel channel) {
		EXECUTOR.submit(() -> {
			try {
				handler.handle(channel);
				handler.loopMessage();
			} catch (IOException e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Client线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
