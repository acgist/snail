package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.utils.Performance;

public class PeerContextTest extends Performance {

	@Test
	public void testNewPeerSession() {
		final String hash = "1".repeat(20);
		this.costed(100000, 100, () -> {
			PeerContext.getInstance().newPeerSession(hash, StatisticsContext.getInstance().statistics(), "192.168.1.100", 1000, Source.CONNECT);
		});
		this.log(PeerContext.getInstance().listPeerSession(hash).size());
		assertTrue(PeerContext.getInstance().listPeerSession(hash).size() == 1);
	}
	
}
