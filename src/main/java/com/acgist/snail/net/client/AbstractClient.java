package com.acgist.snail.net.client;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.handler.message.MessageHandler;
import com.acgist.snail.net.handler.socket.ConnectHandler;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 抽象客户端
 */
public abstract class AbstractClient extends MessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	
	private ExecutorService executor; // 线程池
	protected AsynchronousChannelGroup group;
	
	/**
	 * @param poolSize 线程池大小
	 * @param poolName 线程池名称
	 */
	protected AbstractClient(int poolSize, String poolName) {
		executor = Executors.newFixedThreadPool(poolSize, SystemThreadContext.newThreadFactory(poolName));
	}
	
	/**
	 * 连接：
	 */
	public void connect() {
		try {
			group = AsynchronousChannelGroup.withThreadPool(executor);
			socket = AsynchronousSocketChannel.open(group);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			socket.connect(new InetSocketAddress(SystemConfig.getServerHost(), SystemConfig.getServerPort()), socket, new ConnectHandler());
		} catch (Exception e) {
			LOGGER.error("客户端连接异常", e);
			close();
		}
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		IoUtils.close(group, null, socket);
		if(executor != null) {
			executor.shutdown();
		}
	}
	
}
