package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.BeanUtils;

/**
 * 客户端连接
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TcpAcceptHandler<T extends TcpMessageHandler> implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpAcceptHandler.class);
	
	/**
	 * 消息代理
	 */
	private final Class<T> clazz;
	
	private TcpAcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public static final <T extends TcpMessageHandler> TcpAcceptHandler<T> newInstance(Class<T> clazz) {
		return new TcpAcceptHandler<>(clazz);
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
		LOGGER.debug("客户端连接成功");
		accept(attachment);
		handle(result);
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端连接异常", exc);
	}

	/**
	 * 消息代理
	 */
	private void handle(AsynchronousSocketChannel result) {
		BeanUtils.newInstance(clazz).handle(result);
	}
	
	/**
	 * 接收请求
	 */
	private void accept(AsynchronousServerSocketChannel attachment) {
		attachment.accept(attachment, this);
	}

}