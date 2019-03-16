package com.acgist.snail.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractUdpMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * UDP客户端
 */
public abstract class AbstractUdpClient<T extends AbstractUdpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUdpClient.class);

	private DatagramChannel channel;
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = Executors.newFixedThreadPool(2, SystemThreadContext.newThreadFactory("Application Udp Client Thread"));
	}

	/**
	 * 打开客户端
	 */
	public boolean open() {
		boolean ok = true;
		try {
			this.channel = DatagramChannel.open();
			channel.configureBlocking(false); // 不阻塞
		} catch (IOException e) {
			close();
			ok = false;
			LOGGER.error("UDP打开端口异常", e);
		}
		return ok;
	}

	/**
	 * 绑定消息处理器
	 */
	public void bindMessageHandler(T handler) {
		EXECUTOR.submit(() -> {
			try {
				handler.handle(channel);
			} catch (IOException e) {
				LOGGER.error("消息代理异常", e);
			}
		});
	}
	
	/**
	 * 发送消息
	 */
	public void send(ByteBuffer buffer, SocketAddress address) {
		try {
			channel.send(buffer, address);
		} catch (IOException e) {
			LOGGER.error("发送Udp消息异常", e);;
		}
	}

	/**
	 * 关闭channel
	 */
	public void close() {
		IoUtils.close(channel);
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Client线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
