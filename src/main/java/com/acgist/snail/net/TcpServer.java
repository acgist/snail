package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 服务端超类
 * TODO：BT任务服务端口
 */
public abstract class TcpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	
	private static final AsynchronousChannelGroup GROUP;
	
	/**
	 * 服务端名称
	 */
	private String name;
	
	private AsynchronousServerSocketChannel server;
	
	static {
		AsynchronousChannelGroup group = null;
		try {
			group = AsynchronousChannelGroup.withThreadPool(SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_TCP_SERVER));
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
		LOGGER.info("启动服务端：{}", name);
		boolean ok = true;
		try {
			server = AsynchronousServerSocketChannel.open(GROUP).bind(new InetSocketAddress(host, port));
			server.accept(server, TcpAcceptHandler.newInstance(clazz));
		} catch (Exception e) {
			ok = false;
			LOGGER.error("TCP Server启动异常：{}", this.name, e);
		}
		if(ok) {
//			GROUP.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // 阻止线程关闭
		} else {
			close();
		}
		return ok;
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		LOGGER.info("TCP Server关闭：{}", name);
		IoUtils.close(server);
	}
	
	/**
	 * 关闭Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭TCP Server线程池");
		IoUtils.close(GROUP);
	}
	
}
