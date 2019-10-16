package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>UDP消息代理</p>
 * <p>非线程安全，使用需要保证每一个消息处理器对应的{@linkplain #socketAddress 远程地址}唯一。</p>
 * <p>UDP发送没有超时时间设置，注意：重写{@link #send(ByteBuffer)}和{@link #send(ByteBuffer, int)}时不要出现死循环。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpMessageHandler implements IMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	/**
	 * 是否关闭
	 */
	protected boolean close = false;
	/**
	 * 通道
	 */
	protected DatagramChannel channel;
	/**
	 * 远程地址
	 */
	protected InetSocketAddress socketAddress;
	/**
	 * 消息处理器
	 */
	protected IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * <p>收到消息</p>
	 * <p>使用消息处理器处理消息，如果没有实现消息处理器，请重写该方法。</p>
	 */
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(this.messageCodec == null) {
			throw new NetException("请实现消息处理器");
		}
		this.messageCodec.decode(buffer, socketAddress);
	}
	
	/**
	 * 消息代理
	 */
	public void handle(DatagramChannel channel, InetSocketAddress socketAddress) {
		this.channel = channel;
		this.socketAddress = socketAddress;
	}

	@Override
	public boolean available() {
		return !this.close && this.channel != null;
	}
	
	@Override
	public void send(String message, String charset) throws NetException {
		if(this.messageCodec == null) {
			throw new NetException("请实现消息处理器");
		}
		send(this.charset(this.messageCodec.encode(message), charset));
	}

	/**
	 * {@inheritDoc}
	 * <p>重写请注意使用super方法发送，防止死循环。</p>
	 */
	@Override
	public void send(ByteBuffer buffer) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>重写请注意使用super方法发送，防止死循环。</p>
	 */
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	/**
	 * 关闭资源，标记关闭，不能关闭通道。
	 */
	@Override
	public void close() {
		this.close = true;
	}
	
	/**
	 * <p>发送消息</p>
	 */
	protected void send(ByteBuffer buffer, SocketAddress socketAddress) throws NetException {
		if(!available()) {
			LOGGER.debug("UDP消息发送失败：通道不可用");
			return;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("UDP消息发送失败：{}", buffer);
			return;
		}
		try {
			final int size = this.channel.send(buffer, socketAddress);
			if(size <= 0) {
				LOGGER.warn("UDP消息发送失败：{}-{}", socketAddress, size);
			}
		} catch (IOException e) {
			throw new NetException(e);
		}
	}
	
}
