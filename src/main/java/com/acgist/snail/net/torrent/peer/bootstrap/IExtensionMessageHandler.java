package com.acgist.snail.net.torrent.peer.bootstrap;

import java.nio.ByteBuffer;

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
	void onMessage(ByteBuffer buffer);
	
}
