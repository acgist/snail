package com.acgist.snail.net.peer;

/**
 * 抽象客户端
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
