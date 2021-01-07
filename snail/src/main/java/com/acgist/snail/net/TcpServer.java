package com.acgist.snail.net;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>TCP服务端</p>
 * 
 * @author acgist
 */
public abstract class TcpServer<T extends TcpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	
	/**
	 * <p>服务端线程池</p>
	 */
	private static final AsynchronousChannelGroup GROUP;
	
	static {
		AsynchronousChannelGroup group = null;
		try {
			final var executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_TCP_SERVER);
			group = AsynchronousChannelGroup.withThreadPool(executor);
		} catch (Exception e) {
			LOGGER.error("启动TCP Server Group异常");
		}
		GROUP = group;
	}
	
	/**
	 * <p>服务端名称</p>
	 */
	private final String name;
	/**
	 * <p>消息代理类型</p>
	 */
	private final Class<T> clazz;
	/**
	 * <p>TCP Server</p>
	 */
	private AsynchronousServerSocketChannel server;
	
	/**
	 * <p>TCP服务端</p>
	 * 
	 * @param name 服务端名称
	 * @param clazz 消息代理类型
	 */
	protected TcpServer(String name, Class<T> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	/**
	 * <p>开启监听</p>
	 * 
	 * @return 开启状态
	 */
	public abstract boolean listen();
	
	/**
	 * <p>开启监听</p>
	 * 
	 * @param port 端口
	 * 
	 * @return 开启状态
	 */
	public boolean listen(int port) {
		return this.listen(null, port);
	}
	
	/**
	 * <p>开启监听</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return 开启状态
	 */
	protected boolean listen(String host, int port) {
		LOGGER.debug("启动TCP服务端：{}", this.name);
		boolean success = true;
		try {
			this.server = AsynchronousServerSocketChannel.open(GROUP);
			this.server.bind(NetUtils.buildSocketAddress(host, port));
			this.server.accept(this.server, TcpAcceptHandler.newInstance(this.clazz));
		} catch (Exception e) {
			LOGGER.error("启动TCP服务端异常：{}", this.name, e);
			success = false;
		}
		if(success) {
//			GROUP.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // 阻止线程关闭
		} else {
			close();
		}
		return success;
	}
	
	/**
	 * <p>关闭TCP Server</p>
	 */
	public void close() {
		LOGGER.debug("关闭TCP Server：{}", this.name);
		IoUtils.close(this.server);
	}
	
	/**
	 * <p>关闭TCP Server线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭TCP Server线程池");
		SystemThreadContext.shutdown(GROUP);
	}
	
}
