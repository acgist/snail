package com.acgist.snail.net.torrent;

import java.nio.ByteBuffer;

import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>加密消息代理</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageEncryptHandler extends IMessageHandler {

	/**
	 * 消息加密发送
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	void sendEncrypt(ByteBuffer buffer) throws NetException;
	
}
