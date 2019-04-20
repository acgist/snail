package com.acgist.snail.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.IoUtils;

/**
 * TCP消息处理
 */
public abstract class TcpMessageHandler extends TcpSender implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	protected boolean server = false; // 是否是服务端
	
	protected boolean close = false; // 是否关闭
	
	public TcpMessageHandler() {
	}

	public TcpMessageHandler(String split) {
		super(split);
	}
	
	/**
	 * 处理消息
	 * @return 是否继续循环读取：true-是；false-不继续
	 */
	public abstract void onMessage(ByteBuffer attachment);
	
	/**
	 * 设置为服务端
	 */
	public TcpMessageHandler server() {
		this.server = true;
		return this;
	}
	
	/**
	 * 判断是否是服务端
	 */
	public boolean isServer() {
		return this.server;
	}
	
	/**
	 * 消息代理
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		loopRead();
	}

	/**
	 * 关闭SOCKET
	 */
	public void close() {
		this.close = true;
		IoUtils.close(this.socket);
	}
	
	@Override
	public void completed(Integer result, ByteBuffer attachment) {
//		synchronized (this) {
		if (result == null) {
			this.close();
		} else if(result == -1) { // 服务端关闭
			this.close();
		} else if(result == 0) { // 未遇到过这个情况
			LOGGER.debug("消息长度为零");
		} else {
			onMessage(attachment);
		}
		if(close) {
			LOGGER.debug("TCP消息代理跳出循环：{}", result);
		} else {
			loopRead();
		}
//		}
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		LOGGER.error("消息处理异常", exc);
	}
	
	/**
	 * 循环读
	 */
	private void loopRead() {
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		if(socket.isOpen()) {
			socket.read(buffer, buffer, this);
		}
	}

}
