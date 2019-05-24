package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * 消息代理
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IMessageHandler {

	/**
	 * 关闭
	 */
	void close();
	
	/**
	 * 是否可用
	 */
	boolean available();
	
	/**
	 * 消息发送
	 */
	void send(String message) throws NetException;
	
	/**
	 * 消息发送
	 */
	void send(byte[] bytes) throws NetException;
	
	/**
	 * 消息发送
	 */
	void send(ByteBuffer buffer) throws NetException;

	/**
	 * 获取远程客户端
	 */
	InetSocketAddress remoteSocketAddress();
	
}
