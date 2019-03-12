package com.acgist.snail.net.server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 服务端超类
 * TODO：BT任务服务端口
 */
public abstract class AbstractServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);
	
	private static final ExecutorService EXECUTOR;
	
	private String name;
	private AsynchronousChannelGroup group;
	private AsynchronousServerSocketChannel server;
	
	static {
		EXECUTOR = Executors.newFixedThreadPool(2, SystemThreadContext.newThreadFactory("Application Server Thread"));
	}
	
	/**
	 * 线程大小根据客户类型优化
	 */
	protected AbstractServer(String name) {
		this.name = name;
	}

	/**
	 * 开启监听
	 */
	public abstract boolean listen();
	
	/**
	 * 开启监听
	 */
	public abstract boolean listen(String host, int port);
	
	/**
	 * 开启监听
	 */
	protected boolean listen(String host, int port, AbstractMessageHandler messageHandler) {
		LOGGER.info("启动{}", name);
		boolean ok = true;
		try {
			group = AsynchronousChannelGroup.withThreadPool(EXECUTOR);
			server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(host, port));
			server.accept(server, new AcceptHandler(messageHandler));
		} catch (Exception e) {
			ok = false;
			LOGGER.error("启动{}异常", name, e);
		}
		if(ok) {
			SystemThreadContext.runasyn(() -> {
				try {
					LOGGER.info("启动{}线程", name);
					group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					LOGGER.error("启动{}异常", name, e);
				}
			});
		} else {
			close();
		}
		return ok;
	}

	/**
	 * 关闭资源
	 */
	public void close() {
		LOGGER.info("关闭{}", name);
		IoUtils.close(group, server, null);
	}
	
	/**
	 * 关闭Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
