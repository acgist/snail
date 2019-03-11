package com.acgist.snail.net.client;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.net.socket.impl.ConnectHandler;
import com.acgist.snail.net.socket.impl.WriterHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 抽象客户端
 */
public abstract class AbstractClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	
	private static final ExecutorService EXECUTOR; // 线程池

	static {
		EXECUTOR = Executors.newFixedThreadPool(2, SystemThreadContext.newThreadFactory("Application Client Thread"));
	}
	
	private Lock lock = new ReentrantLock(); // 线程锁
	private Semaphore semaphore = new Semaphore(1); // 信号量
	
	protected AsynchronousChannelGroup group;
	protected AsynchronousSocketChannel socket;
	
	/**
	 * 连接服务端
	 */
	public abstract void connect();
	
	/**
	 * 连接服务端
	 */
	public abstract void connect(String host, int port);
	
	/**
	 * 连接服务端
	 */
	protected void connect(String host, int port, AbstractMessageHandler messageHandler) {
		try {
			group = AsynchronousChannelGroup.withThreadPool(EXECUTOR);
			socket = AsynchronousSocketChannel.open(group);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			socket.connect(new InetSocketAddress(host, port), socket, new ConnectHandler(messageHandler));
		} catch (Exception e) {
			LOGGER.error("客户端连接异常", e);
			close();
		}
	}
	
	/**
	 * 发送消息
	 */
	protected void send(ClientMessage message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		this.lock.lock();
		try {
			semaphore.acquire(); // 每次发送一条消息，防止异常：IllegalMonitorStateException
			WriterHandler handler = new WriterHandler(semaphore);
			socket.write(buffer, buffer, handler);
		} catch (Exception e) {
			LOGGER.error("消息发送异常", e);
		} finally {
			this.lock.unlock();
		}
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		IoUtils.close(group, null, socket);
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
