package com.acgist.snail.system.interfaces;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * Peer消息代理
 * TODO：加密
 * TODO：实现流水线
 */
public interface IPeerMessageHandler {
	
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
	void send(ByteBuffer buffer) throws NetException;

	/**
	 * 获取远程客户端
	 */
	InetSocketAddress remoteSocketAddress();
	
}
