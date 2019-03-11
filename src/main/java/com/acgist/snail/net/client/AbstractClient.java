package com.acgist.snail.net.client;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.AbstractSender;
import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * Aio Socket客户端
 */
public abstract class AbstractClient extends AbstractSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	
	/**
	 * 所有客户端公用一个线程池，线程池大小等于客户端类型数量
	 */
	private static final ExecutorService EXECUTOR;
	
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
	 * @param host 服务端地址
	 * @param port 服务端端口
	 */
	public abstract void connect(String host, int port);
	
	/**
	 * 连接服务端
	 * @param host 服务端地址
	 * @param port 服务端端口
	 * @param messageHandler 消息处理代理
	 */
	protected void connect(String host, int port, AbstractMessageHandler messageHandler) {
		try {
			group = AsynchronousChannelGroup.withThreadPool(EXECUTOR);
			socket = AsynchronousSocketChannel.open(group);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			Future<Void> future = socket.connect(new InetSocketAddress(host, port));
			future.get(5, TimeUnit.SECONDS);
			messageHandler.handler(socket);
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
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
