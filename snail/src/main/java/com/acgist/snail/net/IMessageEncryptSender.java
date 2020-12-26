package com.acgist.snail.net;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>加密消息代理</p>
 * 
 * @author acgist
 */
public interface IMessageEncryptSender extends IMessageSender {

	/**
	 * <p>消息加密发送</p>
	 * 
	 * @param buffer 消息内容
	 * 
	 * @throws NetException 网络异常
	 */
	default void sendEncrypt(ByteBuffer buffer) throws NetException {
		this.sendEncrypt(buffer, TIMEOUT_NONE);
	}
	
	/**
	 * <p>消息加密发送</p>
	 * 
	 * @param buffer 消息内容
	 * @param timeout 超时时间
	 * 
	 * @throws NetException 网络异常
	 */
	void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException;
	
}
