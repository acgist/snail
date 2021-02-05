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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>TCP消息代理</p>
 * 
 * @author acgist
 */
public abstract class TcpMessageHandler implements CompletionHandler<Integer, ByteBuffer>, IMessageSender, IMessageReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	/**
	 * <p>是否关闭</p>
	 */
	private volatile boolean close = false;
	/**
	 * <p>Socket</p>
	 */
	protected AsynchronousSocketChannel socket;
	/**
	 * <p>消息处理器</p>
	 */
	protected IMessageDecoder<ByteBuffer> messageDecoder;

	@Override
	public void onReceive(ByteBuffer buffer) throws NetException {
		if(this.messageDecoder == null) {
			throw new NetException("请设置消息处理器或重新接收消息方法");
		}
		this.messageDecoder.decode(buffer);
	}
	
	/**
	 * <p>消息代理</p>
	 * <p>开始消息轮询</p>
	 * 
	 * @param socket 通道
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		this.loopMessage();
	}
	
	@Override
	public boolean available() {
		return !this.close && this.socket != null && this.socket.isOpen();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>阻塞线程（等待发送完成）：防止多线程同时写导致WritePendingException</p>
	 * <p>超时时间：超时异常会导致数据并没有发送完成而释放了锁从而引起一连串的WritePendingException异常</p>
	 * <p>超时建议：除了第一条消息（连接消息）以外的所有消息都不要使用超时时间</p>
	 */
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.check(buffer);
		synchronized (this.socket) {
			try {
				int size;
				final Future<Integer> future = this.socket.write(buffer);
				if(timeout <= SystemConfig.NONE_TIMEOUT) {
					size = future.get();
				} else {
					size = future.get(timeout, TimeUnit.SECONDS);
				}
				if(size <= 0) {
					LOGGER.warn("TCP消息发送失败：{}-{}", this.socket, size);
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
			return (InetSocketAddress) this.socket.getRemoteAddress();
		} catch (IOException e) {
			LOGGER.error("TCP获取远程服务地址异常", e);
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
		} else if(result == -1) {
			// 服务端关闭
			this.close();
		} else if(result == 0) {
			// 消息空轮询
			LOGGER.debug("TCP消息接收失败（长度）：{}", result);
		} else {
			try {
				buffer.flip();
				this.onReceive(buffer);
			} catch (NetException e) {
				LOGGER.error("TCP消息接收异常", e);
			}
		}
		this.loopMessage();
	}
	
	@Override
	public void failed(Throwable throwable, ByteBuffer buffer) {
		LOGGER.error("TCP消息处理异常", throwable);
	}
	
	/**
	 * <p>消息轮询</p>
	 */
	private void loopMessage() {
		if(this.available()) {
			final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.TCP_BUFFER_LENGTH);
			this.socket.read(buffer, buffer, this);
		} else {
			LOGGER.debug("TCP消息代理退出消息轮询");
		}
	}

}
