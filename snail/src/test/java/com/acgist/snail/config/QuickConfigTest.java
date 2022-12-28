package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.QuickConfig.Type;

class QuickConfigTest {

	@Test
	void testQuickConfig() {
		final Type[] values = QuickConfig.Type.values();
		for (Type type : values) {
			assertEquals(type, Type.of(type.directionType((byte) 0)));
			assertEquals(type, Type.of(type.directionType((byte) 1)));
			assertEquals(type, Type.of(type.directionType((byte) 2)));
			assertEquals(type, Type.of(type.directionType((byte) 4)));
		}
	}
	
}
