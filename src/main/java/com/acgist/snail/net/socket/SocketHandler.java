package com.acgist.snail.net.socket;

import com.acgist.snail.net.message.AbstractMessageHandler;

/**
 * 超类
 */
public abstract class SocketHandler {

	protected AbstractMessageHandler messageHandler;
	
	public SocketHandler() {
	}
	
	public SocketHandler(AbstractMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

}