package com.acgist.snail.net;

import java.io.IOException;
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
 * TODO：多客户端链接异常
 */
public abstract class AbstractTcpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTcpServer.class);
	
	private static final ExecutorService EXECUTOR;
	private static final AsynchronousChannelGroup GROUP;
	
	private String name;
	private AsynchronousServerSocketChannel server;
	
	static {
		EXECUTOR = Executors.newFixedThreadPool(2, SystemThreadContext.newThreadFactory("Application Server Thread"));
		AsynchronousChannelGroup group = null;
		try {
			group = AsynchronousChannelGroup.withThreadPool(EXECUTOR);
		} catch (IOException e) {
			LOGGER.error("启动TCP Server Group异常");
		}
		GROUP = group;
	}
	
	/**
	 * 线程大小根据客户类型优化
	 */
	protected AbstractTcpServer(String name) {
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
			server = AsynchronousServerSocketChannel.open(GROUP).bind(new InetSocketAddress(host, port));
			server.accept(server, new AcceptHandler(messageHandler));
		} catch (Exception e) {
			ok = false;
			LOGGER.error("启动{}异常", name, e);
		}
		if(ok) {
			SystemThreadContext.runasyn(() -> {
				try {
					LOGGER.info("启动{}线程", name);
					GROUP.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
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
		IoUtils.close(server);
	}
	
	/**
	 * 关闭Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭Server线程池");
		IoUtils.close(GROUP);
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
