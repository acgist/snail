package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * 消息发送<br>
 * 发送消息时添加分割信息进行粘包拆包操作
 */
public abstract class TcpSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpSender.class);
	
	/**
	 * 消息分隔符
	 */
	private String split;
	
	protected AsynchronousSocketChannel socket;
	
	public TcpSender() {
	}

	public TcpSender(String split) {
		this.split = split;
	}
	
	/**
	 * 发送消息<br>
	 * 使用分隔符对消息进行分隔
	 */
	protected void send(final String message) {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		try {
			send(splitMessage.getBytes(SystemConfig.DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e) {
			send(splitMessage.getBytes());
			LOGGER.error("编码异常", e);
		}
	}
	
	/**
	 * 发送消息
	 */
	protected void send(byte[] bytes) {
		send(ByteBuffer.wrap(bytes));
	}
	
	/**
	 * 发送消息
	 */
	protected void send(ByteBuffer buffer) {
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		final Future<Integer> future = socket.write(buffer);
		try {
			final int size = future.get(5, TimeUnit.SECONDS); // 阻塞线程防止，防止多线程写入时抛出异常：IllegalMonitorStateException
			if(size <= 0) {
				LOGGER.warn("发送数据为空");
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("发送消息异常", e);
		}
	}

}
