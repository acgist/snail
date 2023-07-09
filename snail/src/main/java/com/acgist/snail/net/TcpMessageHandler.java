package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>TCP消息代理</p>
 * 
 * @author acgist
 */
public abstract class TcpMessageHandler extends MessageHandler<AsynchronousSocketChannel> implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);

	@Override
	public void handle(AsynchronousSocketChannel channel) {
		this.channel = channel;
		this.loopMessage();
	}
	
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.check(buffer);
		// 阻塞线程（等待发送完成）：防止多线程同时写导致WritePendingException
		synchronized (this.channel) {
			try {
				int size;
				final Future<Integer> future = this.channel.write(buffer);
				// 超时时间：超时异常导致数据没有发送完成但释放了锁从而引起一连串的WritePendingException
				if(timeout <= SystemConfig.NONE_TIMEOUT) {
					// 没有超时：除了连接消息（首条消息）以外所有消息都不使用超时时间
					size = future.get();
				} else {
					// 超时时间：连接消息（首条消息）使用超时时间
					size = future.get(timeout, TimeUnit.SECONDS);
				}
				if(size <= 0) {
					LOGGER.warn("TCP消息发送失败：{}-{}", this.channel, size);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new NetException(e);
			} catch (TimeoutException | ExecutionException e) {
				throw new NetException(e);
			}
		}
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		try {
			return (InetSocketAddress) this.channel.getRemoteAddress();
		} catch (IOException e) {
			LOGGER.error("TCP获取远程服务地址异常", e);
		}
		return null;
	}
	
	@Override
	public void close() {
	    if(this.close) {
	        return;
	    }
		LOGGER.debug("TCP连接关闭：{}", this.channel);
		this.close = true;
		IoUtils.close(this.channel);
	}
	
	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		if (result == null) {
			this.close();
		} else if(result == -1) {
			// 服务端关闭
			this.close();
		} else if(result == 0) {
			// 消息空轮询
			LOGGER.debug("TCP消息接收失败（长度）：{}", result);
		} else {
			this.receive(buffer);
		}
		this.loopMessage();
	}
	
	@Override
	public void failed(Throwable throwable, ByteBuffer buffer) {
		LOGGER.error("TCP消息处理异常：{}", this.channel, throwable);
		this.close();
	}
	
	/**
	 * <p>接收消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void receive(ByteBuffer buffer) {
		try {
			if(this.available()) {
				buffer.flip();
				this.onReceive(buffer);
			}
		} catch (Exception e) {
			LOGGER.error("TCP接收消息异常", e);
		}
	}
	
	/**
	 * <p>消息轮询</p>
	 */
	private void loopMessage() {
		if(this.available()) {
			final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.TCP_BUFFER_LENGTH);
			this.channel.read(buffer, buffer, this);
		} else {
			LOGGER.debug("TCP消息代理退出消息轮询");
		}
	}

}
