package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.Performance;

public class SystemContextTest extends Performance {

	@Test
	public void testSystemContext() {
		SystemContext.info();
		this.log(SystemContext.osName());
		assertNotNull(SystemContext.getInstance());
	}
	
	@Test
	public void testLatestRelease() throws NetException {
		assertTrue(SystemContext.latestRelease());
	}
	
	@Test
	public void testBuild() {
		assertDoesNotThrow(() -> SystemContext.build());
	}
	
	@Test
	public void testCosted() {
		this.costed(10, () -> {
			SystemContext.build();
		});
	}
	
}
