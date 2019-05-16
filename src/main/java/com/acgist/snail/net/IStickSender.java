package com.acgist.snail.net;

import java.nio.ByteBuffer;

/**
 * 粘包发送器
 * 
 * @author acgist
 * @since 1.0.2
 */
public interface IStickSender {

	/**
	 * 发送数据包，没有实际发送，调用flush时才会正在发送数据。
	 * 
	 * @param buffer 数据
	 */
	void send(ByteBuffer buffer);
	
	/**
	 * 真正发送数据包
	 * 
	 * @return 发送消息数量
	 */
	int flush();
	
}
