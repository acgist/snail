package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class SystemContextTest extends Performance {

	@Test
	void testSystemContext() {
		SystemContext.info();
		this.log(SystemContext.osName());
		assertNotNull(SystemContext.getInstance());
	}
	
	@Test
	void testLatestRelease() throws NetException {
		assertTrue(SystemContext.latestRelease());
	}
	
	@Test
	void testBuild() {
		assertDoesNotThrow(() -> SystemContext.build());
	}
	
}
