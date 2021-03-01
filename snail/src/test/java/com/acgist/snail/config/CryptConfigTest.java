package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class CryptConfigTest extends Performance {

	@Test
	void testCryptConfig() {
		final CryptConfig.Strategy defaultStrategy = CryptConfig.STRATEGY;
		assertEquals(false, defaultStrategy.crypt());
	}
	
}
