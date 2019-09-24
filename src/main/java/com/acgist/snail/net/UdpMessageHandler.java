package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
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
	 * 收到消息
	 */
	public abstract void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException;
	
	/**
	 * 代理Channel
	 */
	public UdpMessageHandler handle(DatagramChannel channel, InetSocketAddress socketAddress) {
		this.channel = channel;
		this.socketAddress = socketAddress;
		return this;
	}

	@Override
	public boolean available() {
		return !this.close && this.channel != null;
	}
	
	@Override
	public void send(String message) throws NetException {
		send(message, null);
	}

	@Override
	public void send(String message, String charset) throws NetException {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		if(charset == null) {
			send(splitMessage.getBytes());
		} else {
			try {
				send(splitMessage.getBytes(charset));
			} catch (UnsupportedEncodingException e) {
				throw new NetException(String.format("编码异常，编码：%s，内容：%s。", charset, message), e);
			}
		}
	}
	
	@Override
	public void send(byte[] bytes) throws NetException {
		send(ByteBuffer.wrap(bytes));
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		this.send(buffer, this.remoteSocketAddress());
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	/**
	 * 关闭通道：只标记关闭，不关闭通道（UDP通道都是单例）。
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
			LOGGER.debug("发送消息时Channel已经不可用");
			return;
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
				LOGGER.warn("发送数据为空，发送地址：{}", socketAddress);
			}
		} catch (Exception e) {
			throw new NetException(e);
		}
	}
	
}
