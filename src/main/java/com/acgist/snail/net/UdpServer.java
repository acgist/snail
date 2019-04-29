package com.acgist.snail.net;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * UDP服务端
 */
public abstract class UdpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	/**
	 * 服务端名称
	 */
	private final String name;
	private DatagramChannel channel;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}

	public UdpServer(String name) {
		this.name = name;
	}
	
	/**
	 * 开启监听
	 */
	public abstract boolean listen();
	
	/**
	 * 开启监听
	 */
	public boolean listen(int port) {
		return this.listen(null, port);
	}
	
	/**
	 * 开启监听
	 */
	public abstract boolean listen(String host, int port);
	
	/**
	 * 开启监听
	 */
	protected <T extends UdpMessageHandler> boolean listen(String host, int port, Class<T> clazz) {
		final DatagramChannel channel = NetUtils.buildUdpChannel(host, port);
		return listen(channel, clazz);
	}
	
	/**
	 * 打开监听：客户端和服务的使用同一个端口
	 */
	protected <T extends UdpMessageHandler> boolean listen(DatagramChannel channel, Class<T> clazz) {
		LOGGER.info("启动服务端：{}", name);
		if(channel == null) {
			LOGGER.error("UDP Server启动失败：{}", this.name);
			this.close();
			return false;
		}
		this.channel = channel;
		handle(clazz); // 消息代理
		return true;
	}

	/**
	 * 消息处理
	 */
	private <T extends UdpMessageHandler> void handle(Class<T> clazz) {
		EXECUTOR.submit(() -> {
			try {
				BeanUtils.newInstance(clazz).server().handle(channel);
			} catch (IOException e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	public void close() {
		LOGGER.info("UDP Server关闭：{}", this.name);
		IoUtils.close(this.channel);
	}
	
	/**
	 * 关闭Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
