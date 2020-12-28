package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DownloaderInitializerTest extends Performance {

	@Test
	public void testDownloaderInitializer() {
		assertDoesNotThrow(() -> DownloaderInitializer.newInstance().sync());
	}
	
}
