package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class SystemThreadContextTest extends Performance {

	@Test
	void testSystemThreadContext() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		SystemThreadContext.submit(() -> {
			latch.countDown();
		});
		latch.await();
		assertEquals(0, latch.getCount());
		final var executor = SystemThreadContext.newExecutor(2, 10, 100, 10, "ACGIST");
		assertNotNull(executor);
	}
	
	@Test
	void testThreadSize() {
		assertTrue(SystemThreadContext.DEFAULT_THREAD_SIZE > 0);
	}
	
}
