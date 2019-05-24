package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;

/**
 * UDP消息
 * 非线程安全，使用需要保证每一个消息处理器对应的{@linkplain #socketAddress 远程地址}唯一。
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpMessageHandler implements IMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	/**
	 * 消息分隔符
	 */
	private final String split;
	/**
	 * 是否关闭
	 */
	private boolean close = false;
	/**
	 * 通道
	 */
	protected DatagramChannel channel;
	/**
	 * 远程SocketAddress
	 */
	protected InetSocketAddress socketAddress;
	
	public UdpMessageHandler() {
		this(null);
	}

	public UdpMessageHandler(String split) {
		this.split = split;
	}
	
	/**
	 * 消息处理
	 */
	public abstract void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException;
	
	/**
	 * 消息分隔符
	 */
	public String split() {
		return this.split;
	}	
	
	/**
	 * 代理Channel
	 */
	public UdpMessageHandler handle(DatagramChannel channel, InetSocketAddress socketAddress) {
		this.channel = channel;
		this.socketAddress = socketAddress;
		return this;
	}

	@Override
	public void send(String message) throws NetException {
		this.send(message, this.remoteSocketAddress());
	}

	@Override
	public void send(byte[] bytes) throws NetException {
		this.send(bytes, this.remoteSocketAddress());
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	/**
	 * <p>发送消息</p>
	 * <p>使用分隔符对消息进行分隔</p>
	 */
	protected void send(final String message, SocketAddress socketAddress) throws NetException {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		send(splitMessage.getBytes(), socketAddress);
	}
	
	/**
	 * 发送消息
	 */
	protected void send(byte[] bytes, SocketAddress socketAddress) throws NetException {
		send(ByteBuffer.wrap(bytes), socketAddress);
	}
	
	/**
	 * 发送消息
	 */
	protected void send(ByteBuffer buffer, SocketAddress socketAddress) throws NetException {
		if(this.channel == null) {
			throw new NetException("UDP通道没有初始化");
		}
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		// 不用保证顺序
		try {
			final int size = this.channel.send(buffer, socketAddress);
			if(size <= 0) {
				LOGGER.warn("发送数据为空");
			}
		} catch (Exception e) {
			throw new NetException(e);
		}
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}
	
	/**
	 * 可用的：没有被关闭
	 */
	public boolean available() {
		return !close;
	}
	
	/**
	 * 关闭通道，只标记关闭，不关闭通道。
	 */
	public void close() {
		this.close = true;
	}
	
}
