package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;

/**
 * UDP消息接收器。
 */
public abstract class UdpAcceptHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpAcceptHandler.class);

	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newExecutor(4, 10, 10000, 60L, SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * 消息处理
	 * TODO：线程优化
	 */
	public void handle(DatagramChannel channel, ByteBuffer buffer, InetSocketAddress socketAddress) {
		final UdpMessageHandler handler = messageHandler(buffer, socketAddress);
		EXECUTOR.submit(() -> {
			try {
				synchronized (handler) {
					if(handler.available()) {
						handler.handle(channel, socketAddress).onMessage(buffer, socketAddress);
					}
				}
			} catch (Exception e) {
				LOGGER.error("UDP消息处理异常", e);
			}
		});
	}
	
	/**
	 * 获取消息代理
	 * 
	 * @param buffer 消息
	 * @param address 地址
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress);
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP消息处理线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
