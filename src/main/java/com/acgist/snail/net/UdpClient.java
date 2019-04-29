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
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends UdpSender {
	
	public static final String UDP_REGEX = "udp://.*";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	private final T handler;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_CLIENT);
	}
	
	public UdpClient(String name, T handler) {
		this.name = name;
		this.handler = handler;
	}

	/**
	 * 打开客户端
	 */
	public boolean open() {
		final DatagramChannel channel = NetUtils.buildUdpChannel();
		return open(channel);
	}

	/**
	 * 打开客户端：客户端和服务的使用同一个端口
	 */
	protected boolean open(DatagramChannel channel) {
		if(channel == null) {
			return false;
		}
		this.channel = channel;
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
	 * 绑定消息处理器
	 */
	public void handle() {
		EXECUTOR.submit(() -> {
			try {
				this.handler.handle(channel);
			} catch (IOException e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	/**
	 * 关闭channel
	 */
	public void close() {
		LOGGER.debug("UDP Client关闭：{}", this.name);
		IoUtils.close(channel);
	}

	/**
	 * 验证UCP协议
	 */
	public static final boolean verify(String url) {
		return StringUtils.regex(url, UDP_REGEX, true);
	}

	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Client线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
