package com.acgist.snail.net;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>TCP Aio Socket服务端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpServer<T extends TcpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	
	private static final AsynchronousChannelGroup GROUP;
	
	/**
	 * 服务端名称
	 */
	private final String name;
	/**
	 * 消息代理
	 */
	private final Class<T> clazz;
	/**
	 * Socket服务
	 */
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
	protected TcpServer(String name, Class<T> clazz) {
		this.name = name;
		this.clazz = clazz;
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
	protected boolean listen(String host, int port) {
		LOGGER.info("启动服务端：{}", this.name);
		boolean ok = true;
		try {
			this.server = AsynchronousServerSocketChannel.open(GROUP).bind(NetUtils.buildSocketAddress(host, port));
			this.server.accept(this.server, TcpAcceptHandler.newInstance(this.clazz));
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
		LOGGER.info("TCP Server关闭：{}", this.name);
		IoUtils.close(this.server);
	}
	
	/**
	 * 关闭Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭TCP Server线程池");
		IoUtils.close(GROUP);
	}
	
}
