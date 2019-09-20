package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
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
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP服务端</p>
 * <p>全部使用单例，初始化时立即开始监听，客户端使用和服务的同一个通道。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpServer<T extends UdpAcceptHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	private static final int BUFFER_SIZE = 10 * 1024;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * 服务端名称
	 */
	private String name;
	/**
	 * 消息接收器
	 */
	private final T handler;
	/**
	 * 通道
	 */
	protected final DatagramChannel channel;
	
	public UdpServer(int port, String name, T handler) {
		this(NetUtils.buildUdpChannel(port), name, handler);
	}
	
	public UdpServer(DatagramChannel channel, String name, T handler) {
		this.channel = channel;
		this.name = name;
		this.handler = handler;
	}
	
	/**
	 * 多播（组播）
	 */
	public void join(int ttl, String group) {
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
			this.channel.join(InetAddress.getByName(group), NetUtils.defaultNetworkInterface());
		} catch (Exception e) {
			LOGGER.info("UDP多播异常：{}", group, e);
		}
	}
	
	/**
	 * 绑定消息处理
	 */
	public void handler() {
		EXECUTOR.submit(() -> {
			try {
				this.loopMessage();
			} catch (Exception e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	/**
	 * 消息读取
	 */
	private void loopMessage() throws IOException {
		if(this.channel == null) {
			LOGGER.warn("UDP Server通道没有初始化：{}", this.name);
			return;
		}
		final Selector selector = Selector.open();
		this.channel.register(selector, SelectionKey.OP_READ);
		while (this.channel.isOpen()) {
			try {
				if(selector.select() > 0) {
					final Set<SelectionKey> selectionKeys = selector.selectedKeys();
					final Iterator<SelectionKey> selectionKeysIterator = selectionKeys.iterator();
					while (selectionKeysIterator.hasNext()) {
						final SelectionKey selectionKey = selectionKeysIterator.next();
						selectionKeysIterator.remove(); // 移除已经取出来的信息
						if (selectionKey.isValid() && selectionKey.isReadable()) {
							final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
							// final DatagramChannel channel = (DatagramChannel) selectionKey.channel();
							final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
							try {
								this.handler.handle(this.channel, buffer, socketAddress);
							} catch (Exception e) {
								LOGGER.error("UDP消息处理异常", e);
							}
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("UDP消息读取异常", e);
				continue;
			}
		}
	}
	
	/**
	 * 获取UDP通道
	 */
	public DatagramChannel channel() {
		return this.channel;
	}
	
	/**
	 * 关闭UDP通道
	 */
	public void close() {
		LOGGER.info("UDP Server关闭：{}", this.name);
		IoUtils.close(this.channel);
	}
	
	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Server线程池");
		SystemThreadContext.shutdownNow(EXECUTOR);
	}

}
