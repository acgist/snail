package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>消息代理</p>
 * <p>消息发送</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IMessageHandler {
	
	/**
	 * 没有超时时间：一直等待
	 */
	int TIMEOUT_NONE = 0;
	
	/**
	 * 可用状态
	 * 
	 * @return true：可用；false：不可用；
	 */
	boolean available();
	
	/**
	 * 消息发送
	 * 
	 * @param message 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(String message) throws NetException {
		send(message, null);
	}
	
	/**
	 * 消息发送
	 * 
	 * @param message 消息内容
	 * @param charset 编码格式
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @since 1.1.0
	 */
	default void send(String message, String charset) throws NetException {
		send(this.charset(message, charset));
	}
	
	/**
	 * 消息发送
	 * 
	 * @param message 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(byte[] message) throws NetException {
		send(ByteBuffer.wrap(message));
	}
	
	/**
	 * 消息发送
	 * 
	 * @param buffer 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(ByteBuffer buffer) throws NetException {
		send(buffer, TIMEOUT_NONE);
	}
	
	/**
	 * 消息发送（所有其他消息均由这个方法发送）
	 * 
	 * @param buffer 消息内容
	 * @param timeout 超时时间
	 * 
	 * @throws NetException 网络异常
	 */
	void send(ByteBuffer buffer, int timeout) throws NetException;

	/**
	 * 获取远程服务地址
	 * 
	 * @return 远程服务地址
	 */
	InetSocketAddress remoteSocketAddress();
	
	/**
	 * 关闭
	 */
	void close();
	
	/**
	 * 字符编码
	 * 
	 * @param message 消息
	 * @param charset 编码
	 * 
	 * @return 编码后的数据
	 * 
	 * @throws NetException 网络异常
	 */
	default byte[] charset(String message, String charset) throws NetException {
		if(charset == null) {
			return message.getBytes();
		} else {
			try {
				return message.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new NetException(String.format("字符编码失败，编码：%s，内容：%s", charset, message), e);
			}
		}
	}
	
}
