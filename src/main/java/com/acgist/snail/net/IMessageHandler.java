package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * 消息代理：消息接收、发送
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IMessageHandler {

	/**
	 * 是否可用
	 */
	boolean available();
	
	/**
	 * 消息发送
	 * 
	 * @param message 消息内容
	 */
	void send(String message) throws NetException;
	
	/**
	 * 消息发送
	 * 
	 * @param message 消息内容
	 * @param charset 编码格式
	 * 
	 * @since 1.1.0
	 */
	void send(String message, String charset) throws NetException;
	
	/**
	 * 消息发送
	 * 
	 * @param bytes 消息内容
	 */
	void send(byte[] bytes) throws NetException;
	
	/**
	 * 消息发送（所有其他消息均有这个方法发送）
	 * 
	 * @param buffer 消息内容
	 */
	void send(ByteBuffer buffer) throws NetException;

	/**
	 * 获取远程客户端
	 * 
	 * @return 远程客户端/服务端地址
	 */
	InetSocketAddress remoteSocketAddress();
	
	/**
	 * 关闭
	 */
	void close();
	
}
