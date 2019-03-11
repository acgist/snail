package com.acgist.snail.net.socket.impl;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.net.socket.SocketHandler;

/**
 * 客户端连接
 */
public class AcceptHandler extends SocketHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptHandler.class);
	
	public AcceptHandler(AbstractMessageHandler messageHandler) {
		super(messageHandler);
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
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		result.read(buffer, buffer, new ReaderHandler(result, messageHandler));
	}
	
	private void doAccept(AsynchronousServerSocketChannel attachment) {
		attachment.accept(attachment, this);
	}
	
}