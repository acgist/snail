package com.acgist.snail.net.socket.impl;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.net.socket.SocketHandler;

/**
 * 服务器连接
 */
public class ConnectHandler extends SocketHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectHandler.class);
	
	public ConnectHandler(AbstractMessageHandler messageHandler) {
		super(messageHandler);
	}

	@Override
	public void completed(Void result, AsynchronousSocketChannel attachment) {
		LOGGER.info("服务端连接成功");
		doReader(attachment);
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		LOGGER.error("服务器连接异常", exc);
	}
	
	private void doReader(AsynchronousSocketChannel attachment) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		attachment.read(buffer, buffer, new ReaderHandler(attachment, messageHandler));
	}

}
