package com.acgist.snail.net.torrent.peer.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>扩展协议</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IExtensionMessageHandler {

	/**
	 * <p>处理扩展消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	void onMessage(ByteBuffer buffer) throws NetException;
	
}
