package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DownloaderInitializerTest extends Performance {

	@Test
	public void testDownloaderInitializer() {
		assertDoesNotThrow(() -> DownloaderInitializer.newInstance().sync());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> DownloaderInitializer.newInstance().sync());
		assertTrue(costed < 30000);
	}
	
}
