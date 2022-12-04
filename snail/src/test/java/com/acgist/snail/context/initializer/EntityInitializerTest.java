package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.EntityInitializer;
import com.acgist.snail.utils.Performance;

class EntityInitializerTest extends Performance {

	@Test
	void testEntityInitializer() {
		assertDoesNotThrow(() -> EntityInitializer.newInstance().sync());
	}
	
}
