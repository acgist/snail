package com.acgist.snail.net;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;

/**
 * 客户端连接
 */
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptHandler.class);
	
	private AbstractMessageHandler messageHandler;
	
	public AcceptHandler(AbstractMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
		LOGGER.info("客户端连接成功");
		doReader(result);
		doAccept(attachment);
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端连接异常", exc);
	}
	
	private void doReader(AsynchronousSocketChannel result) {
		messageHandler.handler(result);
	}
	
	private void doAccept(AsynchronousServerSocketChannel attachment) {
		attachment.accept(attachment, this);
	}

}