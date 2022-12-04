package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.ConfigInitializer;
import com.acgist.snail.utils.Performance;

class ConfigInitializerTest extends Performance {

	@Test
	void testConfigInitializer() {
		assertDoesNotThrow(() -> ConfigInitializer.newInstance().sync());
	}
	
}
