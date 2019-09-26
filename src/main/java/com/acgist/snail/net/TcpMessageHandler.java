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

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

/**
 * TCP消息代理
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpMessageHandler implements CompletionHandler<Integer, ByteBuffer>, IMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	/**
	 * 发送超时时间
	 */
	private static final int TIMEOUT = 4;
	
	/**
	 * 是否关闭
	 */
	private boolean close = false;
	/**
	 * Socket
	 */
	protected AsynchronousSocketChannel socket;
	/**
	 * 消息处理器
	 */
	protected IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * <p>收到消息</p>
	 * <p>使用消息处理器处理消息，如果没有实现消息处理器，请重写该方法。</p>
	 */
	public void onReceive(ByteBuffer buffer) throws NetException {
		if(this.messageCodec == null) {
			throw new NetException("请实现消息处理器");
		}
		this.messageCodec.decode(buffer);
	}
	
	/**
	 * 消息代理
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		this.loopMessage();
	}
	
	@Override
	public boolean available() {
		return !this.close && this.socket != null;
	}
	
	@Override
	public void send(String message, String charset) throws NetException {
		if(this.messageCodec == null) {
			throw new NetException("请实现消息处理器");
		}
		send(this.charset(this.messageCodec.encode(message), charset));
	}
	
	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(!available()) {
			LOGGER.debug("发送消息时Socket已经不可用");
			return;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		// 防止多线程同时读写导致WritePendingException
		synchronized (this.socket) {
			final Future<Integer> future = this.socket.write(buffer);
			try {
				// 阻塞线程防止，防止多线程写入时抛出异常：IllegalMonitorStateException
				final int size = future.get(TIMEOUT, TimeUnit.SECONDS);
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
			LOGGER.error("TCP远程客户端信息获取异常", e);
		}
		return null;
	}
	
	@Override
	public void close() {
		this.close = true;
		IoUtils.close(this.socket);
	}
	
	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		if (result == null) {
			this.close();
		} else if(result == -1) { // 服务端关闭
			this.close();
		} else if(result == 0) { // 空轮询
			LOGGER.debug("消息长度为零");
		} else {
			try {
				onReceive(buffer);
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
	public void failed(Throwable ex, ByteBuffer buffer) {
		LOGGER.error("消息处理异常", ex);
	}
	
	/**
	 * 消息循环读取
	 */
	private void loopMessage() {
		if(available()) {
			final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.BUFFER_SIZE);
			// 防止多线程同时读写导致WritePendingException
			synchronized (this.socket) {
				this.socket.read(buffer, buffer, this);
			}
		}
	}

}
