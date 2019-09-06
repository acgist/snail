package com.acgist.snail.net;

import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * 加密消息代理
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageCryptHandler extends IMessageHandler {

	/**
	 * 消息加密发送
	 * 
	 * @param buffer 消息内容
	 */
	void cryptSend(ByteBuffer buffer) throws NetException;
	
}
