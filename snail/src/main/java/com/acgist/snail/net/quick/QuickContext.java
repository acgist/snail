package com.acgist.snail.net.quick;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.context.IContext;
import com.acgist.snail.net.IChannelHandler;
import com.acgist.snail.net.MessageHandlerContext;

/**
 * 快传上下文
 * 
 * @author acgist
 */
public final class QuickContext implements IContext, IChannelHandler<DatagramChannel> {

	private static final QuickContext INSTANCE = new QuickContext();
	
	public static final QuickContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 会话ID
	 */
	private short sessionId;
	/**
	 * <p>UDP通道</p>
	 */
	private DatagramChannel channel;
	/**
	 * <p>消息代理上下文</p>
	 */
	private final MessageHandlerContext context;
	/**
	 * 消息代理
	 */
	private final Map<String, QuickMessageHandler> quickMessageHandlers;
	
	private QuickContext() {
		this.sessionId = 0;
		this.context = MessageHandlerContext.getInstance();
		this.quickMessageHandlers = new ConcurrentHashMap<>();
	}

	@Override
	public void handle(DatagramChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * @return 会话ID
	 */
	public short sessionId() {
		synchronized (this) {
			return this.sessionId++;
		}
	}

	/**
	 * @param sessionId 会话ID
	 * @param socketAddress 地址
	 * 
	 * @return 快传消息服务端代理
	 */
	public QuickMessageHandler get(short sessionId, InetSocketAddress socketAddress) {
		// 服务端Key：客户端地址 + sessionId
		final String key = String.valueOf(sessionId);
//		final String key = socketAddress.getHostString() + socketAddress.getPort() + sessionId;
		QuickMessageHandler quickMessageHandler = this.quickMessageHandlers.get(key);
		if(quickMessageHandler != null) {
			return quickMessageHandler;
		}
		quickMessageHandler = new QuickMessageHandler(key, sessionId, socketAddress);
		quickMessageHandler.handle(this.channel);
		// 只需要管理服务端连接
		this.context.newInstance(quickMessageHandler);
		this.quickMessageHandlers.put(key, quickMessageHandler);
		return quickMessageHandler;
	}

	/**
	 * @return 快传消息客户端代理
	 */
	public QuickMessageHandler build() {
		final short sessionId = this.sessionId();
		// 客户端Key：sessionId
		final String key = String.valueOf(sessionId);
		return this.quickMessageHandlers.computeIfAbsent(key, k -> new QuickMessageHandler(key, sessionId));
	}
	
	/**
	 * 移除快传消息代理
	 * 
	 * @param key 键
	 */
	public void remove(String key) {
		this.quickMessageHandlers.remove(key);
	}
	
}
