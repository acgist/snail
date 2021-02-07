package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.TcpMessageHandlerContext;

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
	 * <p>TCP消息代理上下文</p>
	 */
	private final TcpMessageHandlerContext context;
	
	/**
	 * @param clazz 消息代理类型
	 */
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
		this.context = TcpMessageHandlerContext.getInstance();
	}
	
	/**
	 * <p>创建TCP消息接收代理</p>
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
		this.context.newInstance(this.clazz).handle(channel);
	}
	
	@Override
	public void failed(Throwable throwable, AsynchronousServerSocketChannel server) {
		LOGGER.error("TCP连接异常：{}", server, throwable);
	}
	
}