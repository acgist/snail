package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class InitializerTest extends Performance {

	@Test
	public void testInitializer() {
		final AtomicBoolean init = new AtomicBoolean(false);
		new Initializer() {
			@Override
			protected void init() throws Exception {
				init.set(true);
			}
		}.sync();
		assertTrue(init.get());
	}
	
}
