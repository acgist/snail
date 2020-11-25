package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageCodec;

/**
 * <p>UDP消息代理</p>
 * <p>非线程安全：使用需要保证每一个消息处理器对应的{@linkplain #socketAddress 远程地址}唯一</p>
 * <p>注：重写请注意避免循环调用</p>
 * 
 * @author acgist
 */
public abstract class UdpMessageHandler implements IMessageSender, IMessageReceiver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	/**
	 * <p>是否关闭</p>
	 */
	protected volatile boolean close = false;
	/**
	 * <p>UDP通道</p>
	 */
	protected DatagramChannel channel;
	/**
	 * <p>远程地址</p>
	 */
	protected InetSocketAddress socketAddress;
	/**
	 * <p>消息处理器</p>
	 */
	protected IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>使用消息处理器处理消息</p>
	 * <p>如果没有实现消息处理器，请重写该方法。</p>
	 */
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(this.messageCodec == null) {
			throw new NetException("请实现消息处理器");
		}
		this.messageCodec.decode(buffer, socketAddress);
	}
	
	/**
	 * <p>消息代理</p>
	 * 
	 * @param channel 通道
	 * @param socketAddress 地址
	 */
	public void handle(DatagramChannel channel, InetSocketAddress socketAddress) {
		this.channel = channel;
		this.socketAddress = socketAddress;
	}

	@Override
	public boolean available() {
		return !this.close && this.channel != null;
//		return !this.close && this.channel != null && this.channel.isConnected();
	}
	
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>标记关闭：不能关闭通道（UDP通道单例复用）</p>
	 */
	@Override
	public void close() {
		this.close = true;
	}
	
	/**
	 * <p>发送消息</p>
	 * <p>不允许重写，请直接重写{@link #send(ByteBuffer, int)}方法。</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected final void send(ByteBuffer buffer, SocketAddress socketAddress) throws NetException {
		this.check(buffer);
		try {
			// UDP不用加锁
			final int size = this.channel.send(buffer, socketAddress);
			if(size <= 0) {
				LOGGER.warn("UDP消息发送失败：{}-{}", socketAddress, size);
			}
		} catch (IOException e) {
			throw new NetException(e);
		}
	}
	
}
