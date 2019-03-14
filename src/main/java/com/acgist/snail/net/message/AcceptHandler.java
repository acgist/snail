package com.acgist.snail.net.message;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.BeanUtils;

/**
 * 客户端连接
 */
public class AcceptHandler<T extends AbstractMessageHandler> implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptHandler.class);
	
	private Class<T> clazz;
	
	public AcceptHandler(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
		LOGGER.info("客户端连接成功");
		accept(attachment);
		reader(result);
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端连接异常", exc);
	}
	
	private void reader(AsynchronousSocketChannel result) {
		BeanUtils.newInstance(clazz).handler(result);
	}
	
	private void accept(AsynchronousServerSocketChannel attachment) {
		attachment.accept(attachment, this);
	}

}