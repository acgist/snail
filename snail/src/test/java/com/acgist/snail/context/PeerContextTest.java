package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

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
	
	@Test
	public void testHave() {
		final String hash = "1".repeat(20);
		final var peerContext = PeerContext.getInstance();
		SystemThreadContext.timerFixedDelay(0, 10, TimeUnit.MICROSECONDS, () -> {
			peerContext.flushHave(hash);
		});
		final AtomicInteger index = new AtomicInteger();
		this.costed(100000, 100, () -> {
			peerContext.have(hash, index.incrementAndGet());
			ThreadUtils.sleep(2);
		});
	}
	
}
