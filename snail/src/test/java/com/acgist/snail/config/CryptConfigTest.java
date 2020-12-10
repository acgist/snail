package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class CryptConfigTest extends Performance {

	@Test
	public void test() {
		final CryptConfig.Strategy defaultStrategy = CryptConfig.STRATEGY;
		assertNotNull(defaultStrategy);
		assertEquals(false, defaultStrategy.crypt());
	}
	
}
