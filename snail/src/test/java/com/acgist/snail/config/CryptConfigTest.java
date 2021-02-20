package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class CryptConfigTest extends Performance {

	@Test
	void testCryptConfig() {
		final CryptConfig.Strategy defaultStrategy = CryptConfig.STRATEGY;
		assertNotNull(defaultStrategy);
		assertEquals(false, defaultStrategy.crypt());
	}
	
}
