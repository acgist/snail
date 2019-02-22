package com.acgist.snail.module.handler.socket;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.AioUtils;

/**
 * 发送消息
 */
public class WriterHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WriterHandler.class);
	
	Semaphore semaphore;
	
	public WriterHandler(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		release();
		if (result == 0) {
			LOGGER.info("发送空消息");
		} else {
			String content = AioUtils.readContent(attachment);
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