package com.acgist.snail.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;

/**
 * TCP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpMessageHandler extends TcpSender implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	private static final int BUFFER_SIZE = 10 * 1024;
	
	public TcpMessageHandler() {
	}

	public TcpMessageHandler(String split) {
		super(split);
	}
	
	/**
	 * 处理消息
	 * 
	 * @return 是否继续循环读取：true-是；false-不继续
	 */
	public abstract void onMessage(ByteBuffer attachment) throws NetException;
	
	/**
	 * 消息代理
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		loopRead();
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if (result == null) {
			this.close();
		} else if(result == -1) { // 服务端关闭
			this.close();
		} else if(result == 0) { // 未遇到过这个情况
			LOGGER.debug("消息长度为零");
		} else {
			try {
				onMessage(attachment);
			} catch (Exception e) {
				LOGGER.error("TCP消息处理异常", e);
			}
		}
		if(close) {
			LOGGER.debug("TCP消息代理跳出循环：{}", result);
		} else {
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
		final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		if(available()) {
			socket.read(buffer, buffer, this);
		}
	}
	
}
