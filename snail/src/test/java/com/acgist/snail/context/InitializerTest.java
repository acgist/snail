package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class InitializerTest extends Performance {

	@Test
	void testInitializer() {
		final AtomicBoolean init = new AtomicBoolean(false);
		new Initializer("ACGIST") {
			@Override
			protected void init() {
				init.set(true);
			}
		}.sync();
		assertTrue(init.get());
	}
	
}
