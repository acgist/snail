package com.acgist.snail.net;

import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * TCP客户端
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpClient<T extends TcpMessageHandler> extends ClientMessageHandlerAdapter<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
	
	/**
	 * 客户端线程池
	 */
	private static final AsynchronousChannelGroup GROUP;
	
	static {
		AsynchronousChannelGroup group = null;
		try {
			group = AsynchronousChannelGroup.withThreadPool(SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_TCP_CLIENT));
		} catch (Exception e) {
			LOGGER.error("启动TCP Client Group异常", e);
		}
		GROUP = group;
	}
	
	/**
	 * 客户端名称
	 */
	private String name;
	/**
	 * 超时时间
	 */
	private int timeout;
	
	protected TcpClient(String name, int timeout, T handler) {
		super(handler);
		this.name = name;
		this.timeout = timeout;
	}
	
	/**
	 * 连接服务端
	 * 
	 * @return 连接状态
	 */
	public abstract boolean connect();
	
	/**
	 * 连接服务端
	 * 
	 * @param host 服务端地址
	 * @param port 服务端端口
	 */
	protected boolean connect(final String host, final int port) {
		boolean ok = true;
		AsynchronousSocketChannel socket = null;
		try {
			socket = AsynchronousSocketChannel.open(GROUP);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			final Future<Void> future = socket.connect(NetUtils.buildSocketAddress(host, port));
			future.get(this.timeout, TimeUnit.SECONDS);
			this.handler.handle(socket);
		} catch (Exception e) {
			LOGGER.error("TCP客户端连接异常：{}-{}", host, port, e);
			IoUtils.close(socket);
			ok = false;
		}
		return ok;
	}
	
	/**
	 * <p>关闭资源</p>
	 * <p>使用消息代理关闭资源</p>
	 */
	@Override
	public void close() {
		LOGGER.debug("关闭TCP Client：{}", this.name);
		super.close();
	}

	/**
	 * 关闭TCP Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭TCP Client线程池");
		IoUtils.close(GROUP);
	}

}
