package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class UtpServiceTest extends Performance {

	@Test
	void testConnectionId() {
		final var service = UtpService.getInstance();
		for (int index = 0; index < 2 << 16; index++) {
			final short id = service.connectionId();
			assertTrue(id <= Short.MAX_VALUE);
			assertTrue(id >= Short.MIN_VALUE);
		}
	}
	
	@Test
	void testBuildKey() {
		final var service = UtpService.getInstance();
		final var address = new InetSocketAddress(18888);
		assertDoesNotThrow(() -> this.costed(100000, () -> service.buildKey((short) 100, address)));
	}
	
}
