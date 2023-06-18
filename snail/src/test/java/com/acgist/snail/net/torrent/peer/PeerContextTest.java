package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.utils.Performance;

class PeerContextTest extends Performance {

	@Test
	void testNewPeerSession() {
		final String hash = "1".repeat(20);
		this.costed(100000, 100, () -> {
			PeerContext.getInstance().newPeerSession(hash, StatisticsContext.getInstance().getStatistics(), "192.168.1.100", 1000, Source.CONNECT);
		});
		this.log(PeerContext.getInstance().listPeerSession(hash).size());
		assertEquals(1, PeerContext.getInstance().listPeerSession(hash).size());
	}
	
}
