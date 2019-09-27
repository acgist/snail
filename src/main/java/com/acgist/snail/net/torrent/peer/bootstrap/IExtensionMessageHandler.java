package com.acgist.snail.net.torrent.peer.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>扩展协议</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public interface IExtensionMessageHandler {

	/**
	 * 处理扩展消息
	 * 
	 * @param buffer 消息
	 */
	void onMessage(ByteBuffer buffer) throws NetException;
	
}
