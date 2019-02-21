package com.acgist.snail.module.handler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器连接
 */
public class ConnectHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectHandler.class);
	
	public ConnectHandler() {
		LOGGER.info("连接服务端");
	}

	@Override
	public void completed(Void result, AsynchronousSocketChannel attachment) {
		doReader(attachment);
	}

	private void doReader(AsynchronousSocketChannel attachment) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		attachment.read(buffer, buffer, new ReaderHandler(attachment));
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		LOGGER.error("服务器连接", exc);
	}

}
