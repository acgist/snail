package com.acgist.snail.net.peer;

/**
 * Peer客户端<br>
 * 基本协议：TCP
 */
public interface IPeerClient {

	/**
	 * 发送数据
	 */
	void send();
	
	/**
	 * 接收数据
	 */
	void receive();
	
}
