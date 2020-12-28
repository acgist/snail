package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class ConfigInitializerTest extends Performance {

	@Test
	public void testConfigInitializer() {
		assertDoesNotThrow(() -> ConfigInitializer.newInstance().sync());
	}
	
}
