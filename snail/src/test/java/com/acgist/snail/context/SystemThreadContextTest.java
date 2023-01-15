package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class SystemThreadContextTest extends Performance {

	@Test
	void testName() throws IllegalArgumentException, IllegalAccessException {
		final Field[] fields = SystemThreadContext.class.getDeclaredFields();
		for (Field field : fields) {
			if(!field.getName().startsWith("SNAIL")) {
				continue;
			}
//			this.log("字段：{}", field.getName());
			field.setAccessible(true);
			this.log("字段：{}", field.get(SystemThreadContext.class));
		}
	}
	
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
		final Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < 1000; i++) {
			this.log(map.compute("k", (k, v) -> v == null || v >= 99 ? 1 : v + 1));
		}
		assertTrue(SystemThreadContext.DEFAULT_THREAD_SIZE > 0);
		this.log(SystemThreadContext.threadSize(4, 8));
		this.log(SystemThreadContext.threadSize(2, 256));
		this.log(SystemThreadContext.threadSize(128, 256));
	}

	@Test
	void testRejected() throws InterruptedException {
		final int size = 8;
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch down = new CountDownLatch(size - 1);
		final var pool = SystemThreadContext.newExecutor(1, size - 2, 1, 60, "ACGIST");
		for (int index = 0; index < size; index++) {
			assertDoesNotThrow(() -> pool.submit(() -> {
				down.countDown();
				count.incrementAndGet();
				this.log(Thread.currentThread().getName());
				ThreadUtils.sleep(1000);
			}));
		}
		down.await();
		assertEquals(size - 1, count.get());
	}
	
}
