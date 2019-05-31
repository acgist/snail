package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

/**
 * TCP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpMessageHandler implements CompletionHandler<Integer, ByteBuffer>, IMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	private static final int BUFFER_SIZE = 10 * 1024;
	
	/**
	 * 消息分隔符
	 */
	private final String split;
	/**
	 * 是否关闭
	 */
	private boolean close = false;
	/**
	 * Socket
	 */
	protected AsynchronousSocketChannel socket;
	
	public TcpMessageHandler() {
		this(null);
	}

	public TcpMessageHandler(String split) {
		this.split = split;
	}
	
	/**
	 * 处理消息
	 * 
	 * @return 是否继续循环读取：true-是；false-不继续
	 */
	public abstract void onMessage(ByteBuffer attachment) throws NetException;

	/**
	 * 消息分隔符
	 */
	public String split() {
		return this.split;
	}
	
	/**
	 * 消息代理
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		loopMessage();
	}
	
	/**
	 * <p>发送消息</p>
	 * <p>使用分隔符对消息进行分隔</p>
	 */
	@Override
	public void send(final String message) throws NetException {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		send(splitMessage.getBytes());
	}
	
	/**
	 * 发送消息
	 */
	@Override
	public void send(byte[] bytes) throws NetException {
		send(ByteBuffer.wrap(bytes));
	}
	
	/**
	 * 发送消息
	 */
	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(!available()) {
			LOGGER.debug("发送消息时Socket已经不可用");
			return;
		}
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		synchronized (this.socket) { // 保证顺序
			final Future<Integer> future = this.socket.write(buffer);
			try {
				final int size = future.get(4, TimeUnit.SECONDS); // 阻塞线程防止，防止多线程写入时抛出异常：IllegalMonitorStateException
				if(size <= 0) {
					LOGGER.warn("发送数据为空");
				}
			} catch (Exception e) {
				throw new NetException(e);
			}
		}
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		try {
			return (InetSocketAddress) this.socket.getRemoteAddress();
		} catch (IOException e) {
			LOGGER.error("Peer远程客户端信息获取异常", e);
		}
		return null;
	}
	
	/**
	 * 可用的：没有被关闭
	 */
	@Override
	public boolean available() {
		return !close && this.socket != null;
	}
	
	/**
	 * 关闭SOCKET
	 */
	@Override
	public void close() {
		this.close = true;
		IoUtils.close(this.socket);
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
		if(available()) {
			loopMessage();
		} else {
			LOGGER.debug("TCP消息代理跳出循环：{}", result);
		}
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		LOGGER.error("消息处理异常", exc);
	}
	
	/**
	 * 循环读
	 */
	private void loopMessage() {
		if(available()) {
			final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			this.socket.read(buffer, buffer, this);
		}
	}

}
