package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * Aio Socket客户端
 */
public abstract class TcpClient<T extends TcpMessageHandler> extends TcpSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
	
	/**
	 * 所有客户端公用一个线程池，线程池大小等于客户端类型数量
	 */
	private static final AsynchronousChannelGroup GROUP;
	
	private String name;
	private int timeout;
	/**
	 * 消息代理
	 */
	protected T handler;

	static {
		AsynchronousChannelGroup group = null;
		try {
			group = AsynchronousChannelGroup.withThreadPool(SystemThreadContext.systemExecutor());
		} catch (IOException e) {
			LOGGER.error("启动TCP Client Group异常", e);
		}
		GROUP = group;
	}
	
	public TcpClient(String name, int timeout, T handler) {
		super(null);
		this.name = name;
		this.timeout = timeout;
		this.handler = handler;
	}
	
	public TcpClient(String name, int timeout, String split, T handler) {
		super(split);
		this.name = name;
		this.timeout = timeout;
		this.handler = handler;
	}
	
	/**
	 * 连接服务端
	 */
	public abstract boolean connect();
	
	/**
	 * 连接服务端
	 * @param host 服务端地址
	 * @param port 服务端端口
	 */
	protected boolean connect(String host, int port) {
		boolean ok = true;
		try {
			socket = AsynchronousSocketChannel.open(GROUP);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			Future<Void> future = socket.connect(new InetSocketAddress(host, port));
			future.get(this.timeout, TimeUnit.SECONDS);
			handler.handle(socket);
		} catch (Exception e) {
			ok = false;
			LOGGER.error("客户端连接异常", e);
		}
		if(ok) {
			// 连接成功
		} else {
			close();
		}
		return ok;
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		LOGGER.info("关闭TCP Client：{}", this.name);
		IoUtils.close(socket);
	}

	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭Client线程池");
		IoUtils.close(GROUP);
	}
	
}
