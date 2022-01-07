package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.acgist.snail.context.MessageHandlerContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>TCP消息接收代理</p>
 * 
 * @param <T> TCP消息代理类型
 * 
 * @author acgist
 */
public final class TcpAcceptHandler<T extends TcpMessageHandler> implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpAcceptHandler.class);
	
	/**
	 * <p>消息代理类型</p>
	 */
	private final Class<T> clazz;
	/**
	 * <p>消息代理上下文</p>
	 */
	private final MessageHandlerContext context;
	
	/**
	 * @param clazz 消息代理类型
	 */
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
		this.context = MessageHandlerContext.getInstance();
	}
	
	/**
	 * <p>新建TCP消息接收代理</p>
	 * 
	 * @param <T> 消息代理类型
	 * 
	 * @param clazz 消息代理类型
	 * 
	 * @return TCP消息接收代理
	 */
	public static final <T extends TcpMessageHandler> TcpAcceptHandler<T> newInstance(Class<T> clazz) {
		return new TcpAcceptHandler<>(clazz);
	}
	
	@Override
	public void completed(AsynchronousSocketChannel channel, AsynchronousServerSocketChannel server) {
		LOGGER.debug("TCP连接成功：{}", channel);
		server.accept(server, this);
		final T handler = BeanUtils.newInstance(this.clazz);
		handler.handle(channel);
		this.context.newInstance(handler);
	}
	
	@Override
	public void failed(Throwable throwable, AsynchronousServerSocketChannel server) {
		LOGGER.error("TCP连接异常：{}", server, throwable);
	}
	
}