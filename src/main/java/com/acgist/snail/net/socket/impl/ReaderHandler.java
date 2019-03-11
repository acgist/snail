package com.acgist.snail.net.socket.impl;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.net.socket.SocketHandler;

/**
 * 消息读取
 */
public class ReaderHandler extends SocketHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReaderHandler.class);	
	
	private AsynchronousSocketChannel socket;

	public ReaderHandler(AsynchronousSocketChannel socket, AbstractMessageHandler messageHandler) {
		super(messageHandler);
		this.socket = socket;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if(messageHandler != null) {
			if(messageHandler.doMessage(socket, result, attachment)) {
				doReader();
			}
		}
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		LOGGER.error("读取消息异常", exc);
	}
	
	private void doReader() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socket.read(buffer, buffer, this);
	}

}
