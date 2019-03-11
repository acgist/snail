package com.acgist.snail.net.client;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.AbstractSender;
import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 抽象客户端
 */
public abstract class AbstractClient extends AbstractSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	
	private static final ExecutorService EXECUTOR; // 线程池
	
	private AsynchronousChannelGroup group;

	static {
		EXECUTOR = Executors.newFixedThreadPool(2, SystemThreadContext.newThreadFactory("Application Client Thread"));
	}
	
	public AbstractClient(String split) {
		super(split);
	}
	
	/**
	 * 连接服务端
	 */
	public abstract void connect();
	
	/**
	 * 连接服务端
	 */
	public abstract void connect(String host, int port);
	
	/**
	 * 连接服务端
	 */
	protected void connect(String host, int port, AbstractMessageHandler messageHandler) {
		try {
			group = AsynchronousChannelGroup.withThreadPool(EXECUTOR);
			socket = AsynchronousSocketChannel.open(group);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			Future<Void> future = socket.connect(new InetSocketAddress(host, port));
			future.get();
			messageHandler.handler(socket);
		} catch (Exception e) {
			LOGGER.error("客户端连接异常", e);
			close();
		}
	}
	
	protected void send(String message) {
		super.send(message);
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		IoUtils.close(group, null, socket);
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
