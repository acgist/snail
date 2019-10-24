package com.acgist.snail.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP服务端</p>
 * <p>全部使用单例，初始化时立即开始监听，客户端和服务端使用同一个通道。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpServer<T extends UdpAcceptHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * 服务端名称
	 */
	private String name;
	/**
	 * 消息代理
	 */
	private final T handler;
	/**
	 * Selector：每个服务端独立
	 */
	private final Selector selector;
	/**
	 * UDP通道
	 */
	protected final DatagramChannel channel;
	
	public UdpServer(int port, String name, T handler) {
		this(NetUtils.buildUdpChannel(port), name, handler);
	}
	
	public UdpServer(DatagramChannel channel, String name, T handler) {
		this.channel = channel;
		this.name = name;
		this.handler = handler;
		this.selector = NetUtils.buildSelector();
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
	 * 消息代理
	 */
	public void handle() {
		if(this.channel == null) {
			LOGGER.warn("UDP Server通道没有初始化：{}", this.name);
			return;
		}
		if(!this.channel.isOpen()) {
			LOGGER.warn("UDP Server通道已经关闭：{}", this.name);
			return;
		}
		EXECUTOR.submit(() -> {
			this.loopMessage();
		});
	}
	
	/**
	 * 消息轮询
	 */
	private void loopMessage() {
		this.register();
		while (this.channel.isOpen()) {
			this.receive();
		}
	}
	
	/**
	 * <p>注册Selector消息读取</p>
	 */
	private void register() {
		try {
			this.channel.register(this.selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			LOGGER.error("UDP Server注册Selector消息读取异常", e);
		}
	}
	
	/**
	 * <p>接收消息</p>
	 */
	private void receive() {
		try {
			if(this.selector.select() > 0) {
				final Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
				final Iterator<SelectionKey> iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					final SelectionKey selectionKey = iterator.next();
					iterator.remove(); // 移除已经取出来的信息
					if (selectionKey.isValid() && selectionKey.isReadable()) {
						final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.UDP_BUFFER_LENGTH);
						// 单例客户端通道=服务端通道，TCP需要这样获取不同的通道。
						// final DatagramChannel channel = (DatagramChannel) selectionKey.channel();
						final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
						this.handler.handle(this.channel, buffer, socketAddress);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("UDP Server消息接收异常", e);
		}
	}
	
	/**
	 * 获取UDP通道
	 */
	public DatagramChannel channel() {
		return this.channel;
	}
	
	/**
	 * 关闭UDP Server
	 */
	public void close() {
		LOGGER.info("关闭UDP Server：{}", this.name);
		IoUtils.close(this.channel);
		IoUtils.close(this.selector);
	}
	
	/**
	 * 关闭UDP Server线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Server线程池");
		SystemThreadContext.shutdownNow(EXECUTOR);
	}

}
