package com.acgist.snail.net;

import java.io.IOException;
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

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP服务端</p>
 * <p>全部使用单例：初始化时立即开始监听（客户端和服务端使用同一个通道）</p>
 * 
 * @author acgist
 */
public abstract class UdpServer<T extends UdpAcceptHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	/**
	 * <p>TTL：{@value}</p>
	 */
	public static final int UDP_TTL = 2;
	/**
	 * <p>随机端口：{@value}</p>
	 */
	public static final int PORT_AUTO = -1;
	/**
	 * <p>本机地址：{@value}</p>
	 */
	public static final String ADDR_LOCAL = null;
	/**
	 * <p>重用地址：{@value}</p>
	 */
	public static final boolean ADDR_REUSE = true;
	/**
	 * <p>不重用地址：{@value}</p>
	 */
	public static final boolean ADDR_UNREUSE = false;
	/**
	 * <p>UDP服务端消息处理器线程</p>
	 */
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * <p>服务端名称</p>
	 */
	private String name;
	/**
	 * <p>消息接收代理</p>
	 */
	private final T handler;
	/**
	 * <p>Selector：每个服务端独立</p>
	 */
	private final Selector selector;
	/**
	 * <p>UDP通道</p>
	 */
	protected final DatagramChannel channel;

	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：随机端口、本地地址、不重用地址</p>
	 * 
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(String name, T handler) {
		this(PORT_AUTO, ADDR_LOCAL, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：本地地址、不重用地址</p>
	 * 
	 * @param port 端口
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(int port, String name, T handler) {
		this(port, ADDR_LOCAL, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：不重用地址</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(int port, String host, String name, T handler) {
		this(port, host, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：本地地址</p>
	 * 
	 * @param port 端口
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(int port, boolean reuse, String name, T handler) {
		this(port, ADDR_LOCAL, reuse, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(int port, String host, boolean reuse, String name, T handler) {
		this.name = name;
		this.handler = handler;
		this.selector = this.buildSelector();
		this.channel = this.buildChannel(port, host, reuse);
	}
	
	/**
	 * <p>创建Selector</p>
	 * 
	 * @return Selector
	 */
	private Selector buildSelector() {
		try {
			return Selector.open();
		} catch (IOException e) {
			LOGGER.error("创建Selector异常：{}", this.name, e);
		}
		return null;
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param reuse 是否重用地址
	 * 
	 * @return UDP通道
	 * 
	 * @see #buildUdpChannel(int, String, boolean)
	 */
	private DatagramChannel buildChannel(int port, String host, boolean reuse) {
		try {
			return this.buildUdpChannel(port, host, reuse);
		} catch (NetException e) {
			LOGGER.error("创建UDP通道异常：{}", this.name, e);
		}
		return null;
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道绑定：bind：receive、send</p>
	 * <p>通道连接：connect：read、write</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param reuse 是否重用地址
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 */
	private DatagramChannel buildUdpChannel(int port, String host, boolean reuse) throws NetException {
		boolean success = true;
		DatagramChannel udpChannel = null;
		try {
			udpChannel = DatagramChannel.open(NetUtils.LOCAL_PROTOCOL_FAMILY);
			// 不阻塞
			udpChannel.configureBlocking(false);
			if(reuse) {
				udpChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			}
			if(port >= 0) {
				udpChannel.bind(NetUtils.buildSocketAddress(host, port));
			}
		} catch (IOException e) {
			success = false;
			throw new NetException("创建UDP通道失败", e);
		} finally {
			if(success) {
				// 成功
			} else {
				IoUtils.close(udpChannel);
				udpChannel = null;
			}
		}
		return udpChannel;
	}
	
	/**
	 * <p>多播（组播）</p>
	 * 
	 * @param ttl TTL
	 * @param group 分组
	 */
	public void join(int ttl, String group) {
		if(this.channel == null) {
			LOGGER.warn("UDP多播失败（通道没有创建）：{}-{}", this.name, group);
			return;
		}
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
			this.channel.join(InetAddress.getByName(group), NetUtils.DEFAULT_NETWORK_INTERFACE);
		} catch (IOException e) {
			LOGGER.debug("UDP多播异常：{}-{}", this.name, group, e);
		}
	}
	
	/**
	 * <p>消息代理</p>
	 */
	protected void handle() {
		if(this.channel == null) {
			LOGGER.warn("UDP Server通道没有创建：{}", this.name);
			return;
		}
		if(!this.channel.isOpen()) {
			LOGGER.warn("UDP Server通道已经关闭：{}", this.name);
			return;
		}
		EXECUTOR.submit(this::loopMessage);
	}
	
	/**
	 * <p>消息轮询</p>
	 */
	private void loopMessage() {
		this.selector();
		while (this.channel.isOpen()) {
			try {
				this.receive();
			} catch (Exception e) {
				LOGGER.error("UDP Server消息轮询异常：{}", this.name, e);
			}
		}
		LOGGER.debug("UDP Server退出消息轮询：{}", this.name);
	}
	
	/**
	 * <p>注册Selector消息读取事件</p>
	 */
	private void selector() {
		try {
			this.channel.register(this.selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			LOGGER.error("注册Selector消息读取事件异常：{}", this.name, e);
		}
	}
	
	/**
	 * <p>消息接收</p>
	 * 
	 * @throws IOException IO异常
	 */
	private void receive() throws IOException {
		if(this.selector.select() > 0) {
			final Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
			final Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				final SelectionKey selectionKey = iterator.next();
				// 移除已经取出来的信息
				iterator.remove();
				if (selectionKey.isValid() && selectionKey.isReadable()) {
					final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.UDP_BUFFER_LENGTH);
					// 服务器多例：selectionKey.channel()
					// 服务端单例：客户端通道=服务端通道
					final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
					this.handler.receive(this.channel, buffer, socketAddress);
				}
			}
		}
	}
	
	/**
	 * <p>获取UDP通道</p>
	 * 
	 * @return UDP通道
	 */
	public DatagramChannel channel() {
		return this.channel;
	}
	
	/**
	 * <p>关闭UDP Server</p>
	 */
	public void close() {
		LOGGER.debug("关闭UDP Server：{}", this.name);
		IoUtils.close(this.channel);
		IoUtils.close(this.selector);
	}
	
	/**
	 * <p>关闭UDP Server线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭UDP Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}

}
