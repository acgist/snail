package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AcceptHandler;
import com.acgist.snail.net.message.TcpMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 服务端超类
 * TODO：BT任务服务端口
 */
public abstract class TcpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	
	private static final AsynchronousChannelGroup GROUP;
	
	private String name;
	private AsynchronousServerSocketChannel server;
	
	static {
		AsynchronousChannelGroup group = null;
		try {
			group = AsynchronousChannelGroup.withThreadPool(SystemThreadContext.systemExecutor());
		} catch (IOException e) {
			LOGGER.error("启动TCP Server Group异常");
		}
		GROUP = group;
	}
	
	/**
	 * 线程大小根据客户类型优化
	 */
	protected TcpServer(String name) {
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
	protected <T extends TcpMessageHandler> boolean listen(String host, int port, Class<T> clazz) {
		LOGGER.info("启动{}", name);
		boolean ok = true;
		try {
			server = AsynchronousServerSocketChannel.open(GROUP).bind(new InetSocketAddress(host, port));
			server.accept(server, AcceptHandler.newInstance(clazz));
		} catch (Exception e) {
			ok = false;
			LOGGER.error("启动{}异常", name, e);
		}
		if(ok) {
//			阻止线程关闭
//			GROUP.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
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
	}
	
}
