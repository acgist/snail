package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 全部使用单例
 */
public class UdpServer<T extends UdpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	private static final int BUFFER_SIZE = 10 * 1024;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_CLIENT);
	}
	
	/**
	 * 服务端名称
	 */
	private String name;
	private final Class<T> clazz;
	protected final DatagramChannel channel;
	
	public UdpServer(int port, String name, Class<T> clazz) {
		this.channel = NetUtils.buildUdpChannel(port);
		this.name = name;
		this.clazz = clazz;
	}
	
	/**
	 * 绑定消息处理
	 */
	public void handler() {
		EXECUTOR.submit(() -> {
			try {
				this.loopMessage();
			} catch (IOException e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	/**
	 * 循环读取消息
	 */
	private void loopMessage() throws IOException {
		final Selector selector = Selector.open();
		if(this.channel == null) {
			return;
		}
		this.channel.register(selector, SelectionKey.OP_READ);
		while (this.channel.isOpen()) {
			try {
				if(selector.select() > 0) {
					final Set<SelectionKey> keys = selector.selectedKeys();
					final Iterator<SelectionKey> keysIterator = keys.iterator();
					while (keysIterator.hasNext()) {
						final SelectionKey selectedKey = keysIterator.next();
						keysIterator.remove();
						if (selectedKey.isValid() && selectedKey.isReadable()) {
							final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
							final InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);
							try {
								BeanUtils.newInstance(clazz).onMessage(buffer, address);
							} catch (Exception e) {
								LOGGER.error("UDP消息处理异常", e);
							}
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error("UDP消息读取异常", e);
				continue;
			}
		}
	}
	
	public DatagramChannel channel() {
		return this.channel;
	}
	
	public void close() {
		LOGGER.info("DHT Server关闭：{}", this.name);
		IoUtils.close(channel);
	}
	
	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}

}
