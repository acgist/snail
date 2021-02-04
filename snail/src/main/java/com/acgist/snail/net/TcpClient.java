package com.acgist.snail.net;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>TCP客户端</p>
 * 
 * @author acgist
 */
public abstract class TcpClient<T extends TcpMessageHandler> extends ClientMessageHandlerAdapter<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
	
	/**
	 * <p>客户端线程池</p>
	 */
	private static final AsynchronousChannelGroup GROUP;
	
	static {
		AsynchronousChannelGroup group = null;
		try {
			final var executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_TCP_CLIENT);
			group = AsynchronousChannelGroup.withThreadPool(executor);
		} catch (IOException e) {
			LOGGER.error("启动TCP Client Group异常", e);
		}
		GROUP = group;
	}
	
	/**
	 * <p>客户端名称</p>
	 */
	private String name;
	/**
	 * <p>超时时间</p>
	 */
	private int timeout;
	
	/**
	 * <p>TCP客户端</p>
	 * 
	 * @param name 客户端名称
	 * @param timeout 超时时间
	 * @param handler 消息代理
	 */
	protected TcpClient(String name, int timeout, T handler) {
		super(handler);
		this.name = name;
		this.timeout = timeout;
	}
	
	/**
	 * <p>连接服务端</p>
	 * 
	 * @return 连接状态
	 */
	public abstract boolean connect();
	
	/**
	 * <p>连接服务端</p>
	 * 
	 * @param host 服务端地址
	 * @param port 服务端端口
	 * 
	 * @return 连接状态
	 */
	protected boolean connect(final String host, final int port) {
		boolean success = true;
		AsynchronousSocketChannel socket = null;
		try {
			socket = AsynchronousSocketChannel.open(GROUP);
			// TODO：参数调优：TCP_NODELAY
//			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			final Future<Void> future = socket.connect(NetUtils.buildSocketAddress(host, port));
			future.get(this.timeout, TimeUnit.SECONDS);
			this.handler.handle(socket);
		} catch (InterruptedException e) {
			LOGGER.error("TCP客户端连接异常：{}-{}", host, port, e);
			Thread.currentThread().interrupt();
			success = false;
		} catch (IOException | ExecutionException | TimeoutException e) {
			LOGGER.error("TCP客户端连接异常：{}-{}", host, port, e);
			success = false;
		} finally {
			if(success) {
				// 连接成功
			} else {
				IoUtils.close(socket);
				this.handler.close();
			}
		}
		return success;
	}
	
	@Override
	public void close() {
		LOGGER.debug("关闭TCP Client：{}", this.name);
		super.close();
	}

	/**
	 * <p>关闭TCP Client线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭TCP Client线程池");
		SystemThreadContext.shutdown(GROUP);
	}

}
