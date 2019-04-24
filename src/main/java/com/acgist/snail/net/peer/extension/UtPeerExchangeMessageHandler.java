package com.acgist.snail.net.peer.extension;

import java.nio.ByteBuffer;
import java.util.Map;

import com.acgist.snail.system.bcode.BCodeDecoder;

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
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> data = decoder.mustMap();
	}
	
}
