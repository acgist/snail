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
public abstract class AbstractTcpSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTcpSender.class);
	
	/**
	 * 消息分隔符
	 */
	private String split;
	protected AsynchronousSocketChannel socket;
	
	public AbstractTcpSender(String split) {
		if(split == null) {
			split = "";
		}
		this.split = split;
	}
	
	/**
	 * 发送消息<br>
	 * 使用分隔符对消息进行分隔
	 */
	protected void send(String message) {
		String splitMessage = message + split;
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
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Future<Integer> future = socket.write(buffer);
		try {
			future.get(5, TimeUnit.SECONDS); // 阻塞线程防止，防止多线程写入时抛出异常：IllegalMonitorStateException
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("发送消息异常", e);
		}
	}

}
