package com.acgist.snail.net.message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.AbstractSender;

/**
 * 消息处理
 */
public abstract class AbstractMessageHandler extends AbstractSender implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageHandler.class);
	
	public AbstractMessageHandler(String split) {
		super(split);
	}

	/**
	 * 处理消息
	 * @return 是否继续循环读取：true-是；false-不继续
	 */
	public abstract boolean doMessage(Integer result, ByteBuffer attachment);
	
	/**
	 * 消息代理
	 */
	public void handler(AsynchronousSocketChannel socket) {
		this.socket = socket;
		loopRead();
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if(doMessage(result, attachment)) {
			loopRead();
		}
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		LOGGER.error("消息处理异常", exc);
	}
	
	/**
	 * 循环读
	 */
	private void loopRead() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		if(socket.isOpen()) {
			socket.read(buffer, buffer, this);
		}
	}

}
