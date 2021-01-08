package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.context.SystemStatistics;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class PeerManagerTest extends Performance {

	@Test
	public void testNewPeerSession() {
		final String hash = "1".repeat(20);
		this.costed(100000, 100, () -> {
			PeerManager.getInstance().newPeerSession(hash, SystemStatistics.getInstance().statistics(), "192.168.1.100", 1000, Source.CONNECT);
		});
		this.log(PeerManager.getInstance().listPeerSession(hash).size());
		assertTrue(PeerManager.getInstance().listPeerSession(hash).size() == 1);
	}
	
	@Test
	public void testHave() {
		final String hash = "1".repeat(20);
		final var manager = PeerManager.getInstance();
		SystemThreadContext.timerFixedDelay(0, 10, TimeUnit.MICROSECONDS, () -> {
			manager.flushHave(hash);
		});
		final AtomicInteger index = new AtomicInteger();
		this.costed(100000, 100, () -> {
			manager.have(hash, index.incrementAndGet());
			ThreadUtils.sleep(2);
		});
	}
	
}
