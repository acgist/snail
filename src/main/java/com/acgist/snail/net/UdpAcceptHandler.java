package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;

/**
 * UDP消息连接
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpAcceptHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpAcceptHandler.class);

	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newExecutor(4, 10, 10000, 60L, SystemThreadContext.SNAIL_THREAD_UDP_HANDLER);
	}
	
	/**
	 * 消息处理
	 */
	public void handle(DatagramChannel channel, ByteBuffer buffer, InetSocketAddress socketAddress) {
		final UdpMessageHandler handler = messageHandler(buffer, socketAddress);
		EXECUTOR.submit(() -> {
			try {
				synchronized (handler) {
					handler.handle(channel, socketAddress); // 设置代理
					if(handler.available()) {
						handler.onReceive(buffer, socketAddress);
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
	 * @param socketAddress 地址
	 * 
	 * @return 消息代理
	 */
	public abstract UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress);
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP消息处理线程池");
		SystemThreadContext.shutdownNow(EXECUTOR);
	}
	
}
