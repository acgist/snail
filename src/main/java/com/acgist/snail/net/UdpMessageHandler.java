package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>UDP消息</p>
 * <p>非线程安全，使用需要保证每一个消息处理器对应的{@linkplain #socketAddress 远程地址}唯一。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpMessageHandler implements IMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

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
	/**
	 * 编码解码器
	 */
	protected IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * <p>收到消息</p>
	 * <p>使用消息处理器处理消息，如果没有实现消息处理器，请重写该方法。</p>
	 */
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
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
	public void send(String message) throws NetException {
		send(message, null);
	}

	@Override
	public void send(String message, String charset) throws NetException {
		send(this.charset(this.messageCodec.encode(message), charset));
	}
	
	@Override
	public void send(byte[] message) throws NetException {
		send(ByteBuffer.wrap(message));
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
