package com.acgist.snail.net;

import java.io.IOException;
import java.net.StandardSocketOptions;
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
 * @param <T> TCP消息代理类型
 * 
 * @author acgist
 */
public abstract class TcpServer<T extends TcpMessageHandler> extends Server<AsynchronousServerSocketChannel> {

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
		} catch (IOException e) {
			LOGGER.error("启动TCP Server Group异常");
		}
		GROUP = group;
	}
	
	/**
	 * <p>消息代理类型</p>
	 */
	private final Class<T> clazz;
	
	/**
	 * <p>TCP服务端</p>
	 * 
	 * @param name 服务端名称
	 * @param clazz 消息代理类型
	 */
	protected TcpServer(String name, Class<T> clazz) {
		super(name);
		this.clazz = clazz;
	}

	/**
	 * <p>开启监听</p>
	 * 
	 * @return 是否监听成功
	 */
	public abstract boolean listen();
	
	/**
	 * <p>开启监听</p>
	 * 
	 * @param port 端口
	 * 
	 * @return 是否监听成功
	 */
	protected boolean listen(int port) {
		return this.listen(ADDR_LOCAL, port, ADDR_UNREUSE);
	}
	
	@Override
	protected boolean listen(String host, int port, boolean reuse) {
		LOGGER.debug("启动TCP服务端：{}-{}-{}-{}", this.name, host, port, reuse);
		boolean success = true;
		try {
			this.channel = AsynchronousServerSocketChannel.open(GROUP);
			this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, reuse);
			this.channel.bind(NetUtils.buildSocketAddress(host, port));
			this.channel.accept(this.channel, TcpAcceptHandler.newInstance(this.clazz));
		} catch (IOException e) {
			LOGGER.error("启动TCP服务端异常：{}", this.name, e);
			success = false;
		} finally {
			if(success) {
				LOGGER.debug("启动TCP服务端成功：{}", this.name);
			} else {
				this.close();
			}
		}
		return success;
	}
	
	/**
	 * <p>关闭TCP Server</p>
	 */
	public void close() {
		LOGGER.debug("关闭TCP Server：{}", this.name);
		IoUtils.close(this.channel);
	}
	
	/**
	 * <p>关闭TCP Server线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭TCP Server线程池");
		SystemThreadContext.shutdown(GROUP);
	}
	
}
