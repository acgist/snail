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
 */
public final class TcpAcceptHandler<T extends TcpMessageHandler> implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpAcceptHandler.class);
	
	/**
	 * <p>消息代理类型</p>
	 */
	private final Class<T> clazz;
	
	/**
	 * @param clazz 消息代理类型
	 */
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * <p>创建TCP客户端接收代理</p>
	 * 
	 * @param <T> 消息代理泛型
	 * 
	 * @param clazz 消息代理类型
	 * 
	 * @return TCP客户端接收代理
	 */
	public static final <T extends TcpMessageHandler> TcpAcceptHandler<T> newInstance(Class<T> clazz) {
		return new TcpAcceptHandler<>(clazz);
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel channel) {
		LOGGER.debug("客户端连接成功");
		this.accept(channel);
		this.handle(result);
	}
	
	@Override
	public void failed(Throwable throwable, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端连接异常", throwable);
	}
	
	/**
	 * <p>接收连接</p>
	 * 
	 * @param channel 通道
	 */
	private void accept(AsynchronousServerSocketChannel channel) {
		channel.accept(channel, this);
	}

	/**
	 * <p>消息代理</p>
	 * 
	 * @param channel 通道
	 */
	private void handle(AsynchronousSocketChannel channel) {
		BeanUtils.newInstance(this.clazz).handle(channel);
	}

}