package com.acgist.snail.net.message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.impl.ClientMessageHandler;
import com.acgist.snail.net.socket.impl.WriterHandler;
import com.acgist.snail.pojo.message.ClientMessage;

/**
 * 消息处理
 */
public abstract class AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageHandler.class);
	
	private Lock lock = new ReentrantLock(); // 线程锁
	private Semaphore semaphore = new Semaphore(1); // 信号量
	
	/**
	 * 处理消息
	 * @return 是否继续读取：true-是；false-不继续
	 */
	public abstract boolean doMessage(AsynchronousSocketChannel socket, Integer result, ByteBuffer attachment);
	
	/**
	 * 发送消息
	 */
	protected void send(AsynchronousSocketChannel socket, ClientMessage message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		this.lock.lock();
		try {
			semaphore.acquire(); // 每次发送一条消息，防止异常：IllegalMonitorStateException
			WriterHandler handler = new WriterHandler(semaphore);
			socket.write(buffer, buffer, handler);
		} catch (Exception e) {
			LOGGER.error("消息发送异常", e);
		} finally {
			this.lock.unlock();
		}
	}
	
}
