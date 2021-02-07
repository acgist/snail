package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @param clazz 消息代理类型
	 */
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
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
		server.accept(server, this);
		BeanUtils.newInstance(this.clazz).handle(channel);
	}
	
	@Override
	public void failed(Throwable throwable, AsynchronousServerSocketChannel channel) {
		LOGGER.error("TCP消息接收异常", throwable);
	}
	
}