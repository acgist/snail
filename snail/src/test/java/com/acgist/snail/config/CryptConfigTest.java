package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class CryptConfigTest extends Performance {

	@Test
	void testCryptConfig() {
		assertEquals(false, CryptConfig.STRATEGY.getCrypt());
	}
	
}
