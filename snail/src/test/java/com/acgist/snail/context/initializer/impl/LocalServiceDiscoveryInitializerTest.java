package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class LocalServiceDiscoveryInitializerTest extends Performance {

	@Test
	public void testLocalServiceDiscoveryInitializer() {
		assertDoesNotThrow(() -> LocalServiceDiscoveryInitializer.newInstance().sync());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> LocalServiceDiscoveryInitializer.newInstance().sync());
		assertTrue(costed < 30000);
	}
	
}
