package com.acgist.snail.net;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>消息代理</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IMessageHandler {
	
	/**
	 * <p>没有超时时间：{@value}</p>
	 * <p>一直等待</p>
	 */
	int TIMEOUT_NONE = 0;
	
	/**
	 * <p>可用状态</p>
	 * 
	 * @return true-可用；false-禁用；
	 */
	boolean available();
	
	/**
	 * <p>消息发送</p>
	 * 
	 * @param message 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(String message) throws NetException {
		this.send(message, null);
	}
	
	/**
	 * <p>消息发送</p>
	 * 
	 * @param message 消息内容
	 * @param charset 编码格式
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @since 1.1.0
	 */
	default void send(String message, String charset) throws NetException {
		this.send(this.charset(message, charset));
	}
	
	/**
	 * <p>消息发送</p>
	 * 
	 * @param message 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(byte[] message) throws NetException {
		this.send(ByteBuffer.wrap(message));
	}
	
	/**
	 * <p>消息发送</p>
	 * 
	 * @param buffer 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void send(ByteBuffer buffer) throws NetException {
		this.send(buffer, TIMEOUT_NONE);
	}
	
	/**
	 * <p>消息发送</p>
	 * <p>所有其他消息发送均使用此方法发送</p>
	 * 
	 * @param buffer 消息内容
	 * @param timeout 超时时间
	 * 
	 * @throws NetException 网络异常
	 */
	void send(ByteBuffer buffer, int timeout) throws NetException;

	/**
	 * <p>获取远程服务地址</p>
	 * 
	 * @return 远程服务地址
	 */
	InetSocketAddress remoteSocketAddress();
	
	/**
	 * <p>关闭资源</p>
	 */
	void close();
	
	/**
	 * <p>字符编码</p>
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
				// TODO：多行文本
				throw new NetException(String.format("字符编码失败，编码：%s，内容：%s", charset, message), e);
			}
		}
	}
	
}
