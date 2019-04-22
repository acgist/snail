package com.acgist.snail.net.peer.extension;

import java.nio.ByteBuffer;

/**
 * http://www.bittorrent.org/beps/bep_0011.html
 */
public class UtPeerExchangeMessageHandler {
	
	public static final UtPeerExchangeMessageHandler newInstance() {
		return new UtPeerExchangeMessageHandler();
	}
	
	private UtPeerExchangeMessageHandler() {
	}
	
	public void onMessage(ByteBuffer buffer) {
		
	}
	
}
