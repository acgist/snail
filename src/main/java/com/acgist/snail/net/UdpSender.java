package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

public class UdpSender {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	/**
	 * 消息分隔符
	 */
	private final String split;
	/**
	 * 是否关闭
	 */
	protected boolean close = false;
	
	protected DatagramChannel channel;
	
	public UdpSender() {
		this(null);
	}
	
	public UdpSender(String split) {
		this.split = split;
	}

	/**
	 * 发送消息<br>
	 * 使用分隔符对消息进行分隔
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
		if(!channel.isOpen()) {
			LOGGER.debug("发送消息时Socket已经关闭");
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
			final int size = channel.send(buffer, address);
			if(size <= 0) {
				LOGGER.warn("发送数据为空");
			}
		} catch (Exception e) {
			throw new NetException(e);
		}
	}
	
	/**
	 * 可用的：没有被关闭
	 */
	public boolean available() {
		return !close && channel.isOpen();
	}
	
	/**
	 * 关闭SOCKET
	 */
	public void close() {
		this.close = true;
		IoUtils.close(this.channel);
	}
	
}
