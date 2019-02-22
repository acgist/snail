package com.acgist.snail.module.handler.socket;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端接受
 */
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptHandler.class);
	
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
		LOGGER.info("接受客户端连接");
		doReader(result);
		doAccept(attachment);
	}

	private void doReader(AsynchronousSocketChannel result) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		result.read(buffer, buffer, new ReaderHandler(result));
	}

	private void doAccept(AsynchronousServerSocketChannel attachment) {
		attachment.accept(attachment, this);
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel client) {
		LOGGER.error("客户端接受异常", exc);
	}
	
}