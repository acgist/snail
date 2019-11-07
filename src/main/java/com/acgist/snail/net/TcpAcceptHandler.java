package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.BeanUtils;

/**
 * <p>TCP客户端接收代理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TcpAcceptHandler<T extends TcpMessageHandler> implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpAcceptHandler.class);
	
	/**
	 * 消息代理类型
	 */
	private final Class<T> clazz;
	
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public static final <T extends TcpMessageHandler> TcpAcceptHandler<T> newInstance(Class<T> clazz) {
		return new TcpAcceptHandler<>(clazz);
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel channel) {
		LOGGER.debug("客户端连接成功");
		accept(channel);
		handle(result);
	}
	
	@Override
	public void failed(Throwable ex, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端连接异常", ex);
	}

	/**
	 * 消息代理
	 */
	private void handle(AsynchronousSocketChannel channel) {
		BeanUtils.newInstance(this.clazz).handle(channel);
	}
	
	/**
	 * 接收连接
	 */
	private void accept(AsynchronousServerSocketChannel channel) {
		channel.accept(channel, this);
	}

}