package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * UDP发送器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UdpSender {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpSender.class);
	
	/**
	 * 消息分隔符
	 */
	private final String split;
	/**
	 * 通道
	 */
	protected DatagramChannel channel;
	
	public UdpSender() {
		this(null);
	}
	
	public UdpSender(String split) {
		this.split = split;
	}

	/**
	 * <p>发送消息</p>
	 * <p>使用分隔符对消息进行分隔</p>
	 */
	protected void send(final String message, SocketAddress address) throws NetException {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		try {
			send(splitMessage.getBytes(SystemConfig.DEFAULT_CHARSET), address);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("TCP消息编码异常：{}", splitMessage, e);
			send(splitMessage.getBytes(), address);
		}
	}
	
	/**
	 * 发送消息
	 */
	protected void send(byte[] bytes, SocketAddress address) throws NetException {
		send(ByteBuffer.wrap(bytes), address);
	}
	
	/**
	 * 发送消息
	 */
	public void send(ByteBuffer buffer, SocketAddress address) throws NetException {
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
			final int size = this.channel.send(buffer, address);
			if(size <= 0) {
				LOGGER.warn("发送数据为空");
			}
		} catch (Exception e) {
			throw new NetException(e);
		}
	}

}
