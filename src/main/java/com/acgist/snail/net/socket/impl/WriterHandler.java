package com.acgist.snail.net.socket.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.socket.SocketHandler;
import com.acgist.snail.utils.IoUtils;

/**
 * 消息发送<br>
 * 只能一个消息一个消息发送
 */
public class WriterHandler extends SocketHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WriterHandler.class);
	
	private Semaphore semaphore;
	
	public WriterHandler(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		release();
		if (result == 0) {
			LOGGER.info("发送空消息");
		} else {
			String content = IoUtils.readContent(attachment);
			LOGGER.info("发送消息：{}", content);
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		release();
		LOGGER.error("发送消息异常", exc);
	}
	
	private void release() {
		semaphore.release();
	}

}