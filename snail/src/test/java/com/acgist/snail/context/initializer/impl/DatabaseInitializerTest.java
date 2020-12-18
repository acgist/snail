package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DatabaseInitializerTest extends Performance {

	@Test
	public void testConfigInitializer() {
		assertDoesNotThrow(() -> DatabaseInitializer.newInstance().sync());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> DatabaseInitializer.newInstance().sync());
		assertTrue(costed < 10000);
	}
	
}
