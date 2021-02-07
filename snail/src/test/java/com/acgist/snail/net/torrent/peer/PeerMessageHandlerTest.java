package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class PeerMessageHandlerTest extends Performance {

	@Test
	public void testUseless() {
		final PeerMessageHandler handler = new PeerMessageHandler();
		assertFalse(handler.useless());
		assertTrue(handler.useless());
		this.costed(100000, () -> handler.useless());
	}
	
}
